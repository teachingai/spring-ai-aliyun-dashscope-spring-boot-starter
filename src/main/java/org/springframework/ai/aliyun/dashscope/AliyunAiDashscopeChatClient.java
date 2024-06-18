package org.springframework.ai.aliyun.dashscope;

import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.tools.FunctionDefinition;
import com.alibaba.dashscope.tools.ToolCallBase;
import com.alibaba.dashscope.tools.ToolCallFunction;
import com.alibaba.dashscope.tools.ToolFunction;
import com.alibaba.dashscope.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.aliyun.dashscope.util.ApiUtils;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

;

public class AliyunAiDashscopeChatClient
        extends AbstractFunctionCallSupport<Message, GenerationParam, ResponseEntity<GenerationResult>>
        implements ChatClient, StreamingChatClient {

    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * Default options to be used for all chat requests.
     */
    private AliyunAiDashscopeChatOptions defaultOptions;

    private final com.alibaba.dashscope.aigc.generation.Generation generation;
    public final RetryTemplate retryTemplate;

    public AliyunAiDashscopeChatClient(com.alibaba.dashscope.aigc.generation.Generation generation) {
        this(generation, AliyunAiDashscopeChatOptions.builder()
                        .withTemperature(0.95f)
                        .withTopP(0.7f)
                        //.withModel(ZhipuAiApi.ChatModel.GLM_3_TURBO.getValue())
                        .build());
    }

    public AliyunAiDashscopeChatClient(com.alibaba.dashscope.aigc.generation.Generation generation, AliyunAiDashscopeChatOptions options) {
        this(generation, options, null, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public AliyunAiDashscopeChatClient(com.alibaba.dashscope.aigc.generation.Generation generation, AliyunAiDashscopeChatOptions options, FunctionCallbackContext functionCallbackContext, RetryTemplate retryTemplate) {
        super(functionCallbackContext);
        Assert.notNull(generation, "Generation must not be null");
        Assert.notNull(options, "Options must not be null");
        this.generation = generation;
        this.retryTemplate = retryTemplate;
        this.defaultOptions = options;
    }

    @Override
    public ChatResponse call(Prompt prompt) {

        var request = createRequest(prompt);

        return retryTemplate.execute(ctx -> {

            ResponseEntity<GenerationResult> completionEntity = this.callWithFunctionSupport(request);

            var chatCompletion = completionEntity.getBody();
            if (chatCompletion == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return new ChatResponse(List.of());
            }

            List<Generation> generations = chatCompletion.getOutput().getChoices()
                    .stream()
                    .map(choice -> new Generation(choice.getMessage().getContent(), ApiUtils.toMap(chatCompletion.getRequestId(), choice))
                            .withGenerationMetadata(ChatGenerationMetadata.from(choice.getFinishReason(), ApiUtils.extractUsage(chatCompletion))))
                    .toList();
            return new ChatResponse(generations);
        });
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        var request = createRequest(prompt);
        return retryTemplate.execute(ctx -> Flux.create(sink -> {
            try {
                generation.streamCall(request, new AliyunAiDashscopeResultCallback(sink));
            } catch (ApiException | NoApiKeyException | InputRequiredException e) {
                sink.error(e);
            }
        }));
    }

    /**
     * Accessible for testing.
     */
    GenerationParam createRequest(Prompt prompt) {

        Set<String> functionsForThisRequest = new HashSet<>();

        var request = GenerationParam.builder()
                .messages(ApiUtils.toConversationMessage(prompt.getInstructions()))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        if (this.defaultOptions != null) {
            Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(this.defaultOptions,
                    !IS_RUNTIME_CALL);
            functionsForThisRequest.addAll(defaultEnabledFunctions);
            BeanUtils.copyProperties(this.defaultOptions, request);
        }

        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ChatOptions runtimeOptions) {
                var updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(runtimeOptions, ChatOptions.class,
                        AliyunAiDashscopeChatOptions.class);

                Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(updatedRuntimeOptions,
                        IS_RUNTIME_CALL);
                functionsForThisRequest.addAll(promptEnabledFunctions);

                BeanUtils.copyProperties(updatedRuntimeOptions, request);
            }
            else {
                throw new IllegalArgumentException("Prompt options are not of type ChatOptions: "
                        + prompt.getOptions().getClass().getSimpleName());
            }
        }

        // Add the enabled functions definitions to the request's tools parameter.
        if (!CollectionUtils.isEmpty(functionsForThisRequest)) {
            BeanUtils.copyProperties(AliyunAiDashscopeChatOptions.builder().withTools(this.getFunctionTools(functionsForThisRequest)).build(), request);
        }

        return request;
    }

    private List<ToolFunction> getFunctionTools(Set<String> functionNames) {
        return (List<ToolFunction>) this.resolveFunctionCallbacks(functionNames).stream().map(functionCallback -> {
            var function = FunctionDefinition.builder()
                    .name(functionCallback.getName())
                    .description(functionCallback.getDescription())
                    .parameters(StringUtils.hasText(functionCallback.getInputTypeSchema()) ? JsonUtils.parseString(functionCallback.getInputTypeSchema()).getAsJsonObject() : null)
                    .build();
            return ToolFunction.builder().function(function).build();
        }).toList();
    }


    //
    // Function Calling Support
    //


    public static final String FUNCTION = "function";
    public static final String CODE_INTERPRETER = "code_interpreter";
    public static final String QUARK_SEARCH = "quark_search";

    @Override
    protected GenerationParam doCreateToolResponseRequest(GenerationParam previousRequest, Message responseMessage, List<Message> conversationHistory) {
        // Every tool-call item requires a separate function call and a response (TOOL)
        // message.
        for (ToolCallBase toolCall : responseMessage.getToolCalls()) {

            switch (toolCall.getType()) {
                case FUNCTION:
                    ToolCallFunction toolCallFunction = (ToolCallFunction) toolCall;
                    var functionName = toolCallFunction.getFunction().getName();
                    if (!this.functionCallbackRegister.containsKey(functionName)) {
                        throw new IllegalStateException("No function callback found for function name: " + functionName);
                    }
                    var functionArguments = toolCallFunction.getFunction().getArguments();
                    var functionResponse = this.functionCallbackRegister.get(functionName).call(functionArguments);
                    var functionId = toolCallFunction.getId();
                    // Add the function response to the conversation.
                    conversationHistory.add(Message.builder()
                            .toolCallId(functionId)
                            .name(functionName)
                            .role(Role.TOOL.getValue())
                            .content(functionResponse).build());
            }
        }

        // Recursively call chatCompletionWithTools until the model doesn't call a
        // functions anymore.
        GenerationParam newRequest = GenerationParam.builder().messages(conversationHistory).build();
        BeanUtils.copyProperties(this.defaultOptions, newRequest);

        return newRequest;
    }

    @Override
    protected List<Message> doGetUserMessages(GenerationParam request) {
        return request.getMessages();
    }

    @Override
    protected Message doGetToolResponseMessage(ResponseEntity<GenerationResult> chatCompletion) {
        return chatCompletion.getBody().getOutput().getChoices().iterator().next().getMessage();
    }

    @Override
    protected ResponseEntity<GenerationResult> doChatCompletion(GenerationParam request) {
        try {
            return ResponseEntity.ofNullable(generation.call(request));
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean isToolFunctionCall(ResponseEntity<GenerationResult> chatCompletion) {
        var body = chatCompletion.getBody();
        if (Objects.isNull(body)) {
            return false;
        }
        if (Objects.isNull(body.getOutput())) {
            return false;
        }
        var choices = body.getOutput().getChoices();
        if (CollectionUtils.isEmpty(choices)) {
            return false;
        }
        if (Objects.isNull(choices.get(0).getMessage())) {
            return false;
        }
        return !CollectionUtils.isEmpty(choices.get(0).getMessage().getToolCalls());
    }

}
