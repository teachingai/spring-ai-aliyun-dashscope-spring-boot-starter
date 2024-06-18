package org.springframework.ai.aliyun.dashscope;

import com.alibaba.dashscope.tools.ToolFunction;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallingOptions;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AliyunAiDashscopeChatOptions implements FunctionCallingOptions, ChatOptions {

    public static final Float DEFAULT_TEMPERATURE = 0.95F;

    /**
     * 指定用于对话的通义千问模型名，目前可选择 qwen-turbo、qwen-plus、qwen-max、qwen-max-0403、qwen-max-0107、qwen-max-1201和qwen-max-longcontext。
     */
    @JsonProperty("model")
    private String model;

    /**
     * 生成时使用的随机数种子，用于控制模型生成内容的随机性。seed支持无符号64位整数。
     */
    @JsonProperty("seed")
    private Integer seed;

    /**
     * 指定模型最大输出token数
     * - qwen-turbo最大值和默认值为1500 tokens。
     * - qwen-max、qwen-max-1201、qwen-max-longcontext和qwen-plus模型，最大值和默认值均为2000 tokens。
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * 生成过程中的核采样方法概率阈值，例如，取值为0.8时，仅保留概率加起来大于等于0.8的最可能token的最小集合作为候选集。
     * 取值范围为（0,1.0)，取值越大，生成的随机性越高；取值越低，生成的确定性越高。
     */
    @JsonProperty("top_p")
    private Float topP;

    /**
     * 生成时，采样候选集的大小。例如，取值为50时，仅将单次生成中得分最高的50个token组成随机采样的候选集。取值越大，生成的随机性越高；取值越小，生成的确定性越高。取值为None或当top_k大于100时，表示不启用top_k策略，此时，仅有top_p策略生效。
     */
    @JsonProperty("top_k")
    private Integer topK;

    /**
     * 用于控制模型生成时连续序列中的重复度。提高repetition_penalty时可以降低模型生成的重复度，1.0表示不做惩罚。没有严格的取值范围。
     */
    @JsonProperty(value = "repetition_penalty")
    private Float repetitionPenalty;

    /**
     * 用户控制模型生成时整个序列中的重复度。提高presence_penalty时可以降低模型生成的重复度，取值范围[-2.0, 2.0]。
     */
    @JsonProperty(value = "presence_penalty")
    private Float presencePenalty;

    /**
     * 用于控制模型回复的随机性和多样性。具体来说，temperature值控制了生成文本时对每个候选词的概率分布进行平滑的程度。
     * 较高的temperature值会降低概率分布的峰值，使得更多的低概率词被选择，生成结果更加多样化；
     * 而较低的temperature值则会增强概率分布的峰值，使得高概率词更容易被选择，生成结果更加确定。
     * 取值范围：[0, 2)，不建议取值为0，无意义。
     */
    @JsonProperty("temperature")
    private Float temperature = DEFAULT_TEMPERATURE;

    /**
     * 终端用户的唯一ID，协助平台对终端用户的违规行为、生成违法及不良信息或其他滥用行为进行干预。ID长度要求：最少6个字符，最多128个字符。
     */
    @JsonProperty(value = "user")
    private String user;

    /**
     * 生成停止标识，当模型生成结果以stop中某个元素结尾时，停止文本生成
     */
    @JsonProperty("stopStrings")
    private List<String> stop;

    /**
     * 用于控制模型在生成文本时是否使用互联网搜索结果进行参考。取值如下：
     * True：启用互联网搜索，模型会将搜索结果作为文本生成过程中的参考信息，但模型会基于其内部逻辑判断是否使用互联网搜索结果。
     * False（默认）：关闭互联网搜索。
     */
    @JsonProperty(value = "enable_search")
    private Boolean enableSearch;

    /**
     * 用于指定返回结果的格式，默认为text，也可选择message。当设置为message时，输出格式请参考返回结果。推荐您优先使用message格式。
     */
    @JsonProperty(value = "result_format")
    private String responseFormat = "message";

    /**
     * 控制在流式输出模式下是否开启增量输出，即后续输出内容是否包含已输出的内容。
     * 设置为True时，将开启增量输出模式，后面输出不会包含已经输出的内容，您需要自行拼接整体输出；
     * 设置为False则会包含已输出的内容。
     */
    @JsonProperty(value = "incremental_output")
    private Boolean incrementalOutput;

    /**
     * 用于指定可供模型调用的工具库，一次function call流程模型会从中选择其中一个工具。
     */
    @NestedConfigurationProperty
    private @JsonProperty("tools") List<ToolFunction> tools;

    /**
     * 在使用tools参数时，用于控制模型调用指定工具。有四种取值：
     *
     *     none表示不调用工具。tools参数为空时，默认值为none。
     *     auto表示模型判断是否调用工具，可能调用也可能不调用。tools参数不为空时，默认值为auto。
     *     object结构可以指定模型调用指定工具。例如{"type": "function", "function": {"name": "user_function"}}
     *         type现在只支持function
     *         function
     *             name表示期望被调用的工具名称
     */
    @NestedConfigurationProperty
    private @JsonProperty("tool_choice") Object toolChoice;

    @Override
    public List<FunctionCallback> getFunctionCallbacks() {
        return null;
    }

    @Override
    public void setFunctionCallbacks(List<FunctionCallback> functionCallbacks) {

    }

    @Override
    public Set<String> getFunctions() {
        return null;
    }

    @Override
    public void setFunctions(Set<String> functions) {

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final AliyunAiDashscopeChatOptions options = new AliyunAiDashscopeChatOptions();

        public Builder withModel(String model) {
            this.options.setModel(model);
            return this;
        }

        public Builder withSeed(Integer seed) {
            this.options.seed = seed;
            return this;
        }

        public Builder withMaxTokens(Integer maxTokens) {
            this.options.setMaxTokens(maxTokens);
            return this;
        }

        public Builder withTemperature(Float temperature) {
            this.options.setTemperature(temperature);
            return this;
        }

        public Builder withTopP(Float topP) {
            this.options.setTopP(topP);
            return this;
        }

        public Builder withTopK(Integer topK) {
            this.options.setTopK(topK);
            return this;
        }

        public Builder withRepetitionPenalty(Float repetitionPenalty) {
            this.options.repetitionPenalty = repetitionPenalty;
            return this;
        }

        public Builder withPresencePenalty(Float presencePenalty) {
            this.options.presencePenalty = presencePenalty;
            return this;
        }

        public Builder withUser(String user) {
            this.options.setUser(user);
            return this;
        }

        public Builder withStop(List<String> stop) {
            this.options.setStop(stop);
            return this;
        }

        public Builder withEnableSearch(Boolean enableSearch) {
            this.options.enableSearch = enableSearch;
            return this;
        }

        public Builder withResponseFormat(String responseFormat) {
            this.options.responseFormat = responseFormat;
            return this;
        }

        public Builder withIncrementalOutput(Boolean incrementalOutput) {
            this.options.incrementalOutput = incrementalOutput;
            return this;
        }

        public Builder withTools(List<ToolFunction> tools) {
            this.options.tools = tools;
            return this;
        }

        public Builder withToolChoice(Object toolChoice) {
            this.options.toolChoice = toolChoice;
            return this;
        }

        public AliyunAiDashscopeChatOptions build() {
            return this.options;
        }

    }

    @Override
    public Float getTopP() {
        return this.topP;
    }

    public void setTopP(Float topP) {
        this.topP = topP;
    }

    @Override
    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Float getRepetitionPenalty() {
        return repetitionPenalty;
    }

    public void setRepetitionPenalty(Float repetitionPenalty) {
        this.repetitionPenalty = repetitionPenalty;
    }

    public Float getPresencePenalty() {
        return presencePenalty;
    }

    public void setPresencePenalty(Float presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    @Override
    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<String> getStop() {
        return stop;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
    }

    public Boolean getEnableSearch() {
        return enableSearch;
    }

    public void setEnableSearch(Boolean enableSearch) {
        this.enableSearch = enableSearch;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public Boolean getIncrementalOutput() {
        return incrementalOutput;
    }

    public void setIncrementalOutput(Boolean incrementalOutput) {
        this.incrementalOutput = incrementalOutput;
    }

    public List<ToolFunction> getTools() {
        return tools;
    }

    public void setTools(List<ToolFunction> tools) {
        this.tools = tools;
    }

    public Object getToolChoice() {
        return toolChoice;
    }

    public void setToolChoice(Object toolChoice) {
        this.toolChoice = toolChoice;
    }

    /**
     * Convert the {@link AliyunAiDashscopeChatOptions} object to a {@link Map} of key/value pairs.
     * @return The {@link Map} of key/value pairs.
     */
    public Map<String, Object> toMap() {
        try {
            var json = new ObjectMapper().writeValueAsString(this);
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
            });
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper factory method to create a new {@link AliyunAiDashscopeChatOptions} instance.
     * @return A new {@link AliyunAiDashscopeChatOptions} instance.
     */
    public static AliyunAiDashscopeChatOptions create() {
        return new AliyunAiDashscopeChatOptions();
    }

    /**
     * Filter out the non supported fields from the options.
     * @param options The options to filter.
     * @return The filtered options.
     */
    public static Map<String, Object> filterNonSupportedFields(Map<String, Object> options) {
        return options.entrySet().stream()
                .filter(e -> !e.getKey().equals("model"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


}
