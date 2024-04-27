package org.springframework.ai.aliyun.dashscope.autoconfigure;

import org.springframework.ai.aliyun.dashscope.AliyunAiDashscopeChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(AliyunAiDashscopeChatProperties.CONFIG_PREFIX)
public class AliyunAiDashscopeChatProperties {

    public static final String CONFIG_PREFIX = "spring.ai.aliyunai.dashscope.chat";

    public static final String DEFAULT_CHAT_MODEL = "qwen-turbo";

    private static final Float DEFAULT_TEMPERATURE = 0.8f;

    private static final Float DEFAULT_TOP_P = 0.8f;

    /**
     * Enable 百度千帆 chat client.
     */
    private boolean enabled = true;

    /**
     * Client lever 百度千帆 options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private AliyunAiDashscopeChatOptions options = AliyunAiDashscopeChatOptions.builder()
            .withModel(DEFAULT_CHAT_MODEL)
            .withTemperature(DEFAULT_TEMPERATURE)
            .withTopP(DEFAULT_TOP_P)
            .build();

    public AliyunAiDashscopeChatOptions getOptions() {
        return this.options;
    }

    public void setOptions(AliyunAiDashscopeChatOptions options) {
        this.options = options;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
