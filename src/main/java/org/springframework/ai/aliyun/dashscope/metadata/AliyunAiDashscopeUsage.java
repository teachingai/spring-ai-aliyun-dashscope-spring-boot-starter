package org.springframework.ai.aliyun.dashscope.metadata;

import com.alibaba.dashscope.aigc.generation.GenerationUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

public class AliyunAiDashscopeUsage implements Usage {

    public static AliyunAiDashscopeUsage from(GenerationUsage usage) {
        return new AliyunAiDashscopeUsage(usage);
    }

    private final GenerationUsage usage;

    protected AliyunAiDashscopeUsage(GenerationUsage usage) {
        Assert.notNull(usage, "GenerationUsage must not be null");
        this.usage = usage;
    }

    protected GenerationUsage getUsage() {
        return this.usage;
    }

    @Override
    public Long getPromptTokens() {
        return getUsage().getInputTokens().longValue();
    }

    @Override
    public Long getGenerationTokens() {
        return getUsage().getOutputTokens().longValue();
    }

    @Override
    public Long getTotalTokens() {
        return getUsage().getTotalTokens().longValue();
    }

    @Override
    public String toString() {
        return getUsage().toString();
    }

}
