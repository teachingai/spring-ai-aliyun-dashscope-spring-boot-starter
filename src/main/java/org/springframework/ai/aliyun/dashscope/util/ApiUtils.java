package org.springframework.ai.aliyun.dashscope.util;

import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Role;
import org.springframework.ai.aliyun.dashscope.metadata.AliyunAiDashscopeChatResponseMetadata;
import org.springframework.ai.aliyun.dashscope.metadata.AliyunAiDashscopeUsage;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ApiUtils {

    public static final Float DEFAULT_TEMPERATURE = 0.95f;
    public static final Float DEFAULT_TOP_P = 1.0f;

    public static List<com.alibaba.dashscope.common.Message> toConversationMessage(List<Message> messages){
        if(Objects.isNull(messages)){
            return Collections.emptyList();
        }
        // Build ConversationMessage list from the prompt.
        return messages.stream()
                .filter(message -> message.getMessageType() == MessageType.USER
                        || message.getMessageType() == MessageType.ASSISTANT
                        || message.getMessageType() == MessageType.SYSTEM
                        || message.getMessageType() == MessageType.FUNCTION)
                .map(m -> com.alibaba.dashscope.common.Message.builder().role(ApiUtils.toRole(m).getValue()).content(m.getContent()).build())
                .collect(Collectors.toList());
    }

    public static Role toRole(Message message) {
        switch (message.getMessageType()) {
            case USER:
                return Role.USER;
            case ASSISTANT:
                return Role.ASSISTANT;
            case SYSTEM:
                return Role.SYSTEM;
            case FUNCTION:
                return Role.TOOL;
            default:
                throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
        }
    }

    public static List<com.alibaba.dashscope.common.Message> toChatMessage(List<Message> messages){
        List<com.alibaba.dashscope.common.Message> conversationMessages = new ArrayList<>();
        for (Message message : messages) {
            conversationMessages.add(com.alibaba.dashscope.common.Message.builder()
                    .role(toRole(message).getValue())
                    .content(message.getContent())
                    .build());
        }
        return conversationMessages;
    }

    public static Map<String, Object> toMap(String id, GenerationOutput.Choice choice) {
        Map<String, Object> map = new HashMap<>();

        var message = choice.getMessage();
        if (message.getRole() != null) {
            map.put("role", message.getRole());
        }
        map.put("finishReason", "");
        map.put("id", id);
        return map;
    }

    public static ChatResponse toChatCompletion(GenerationResult resp) {
        Assert.notNull(resp, "GenerationResult must not be null");

        resp.getOutput().getChoices().forEach(choice -> {
            if (StringUtils.isEmpty(choice.getMessage().getContent())) {
                throw new IllegalArgumentException("Choice message content is empty");
            }
        });

        List<Generation> generations = resp.getOutput().getChoices()
                .stream()
                .map(choice -> new Generation(choice.getMessage().getContent(), ApiUtils.toMap(resp.getRequestId(), choice))
                        .withGenerationMetadata(ChatGenerationMetadata.from("chat.completion", ApiUtils.extractUsage(resp))))
                .toList();
        return new ChatResponse(generations, AliyunAiDashscopeChatResponseMetadata.from(resp));
    }

    public static Usage extractUsage(GenerationResult response) {
        return AliyunAiDashscopeUsage.from(response.getUsage());
    }

}
