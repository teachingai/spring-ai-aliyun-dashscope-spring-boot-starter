package org.springframework.ai.aliyun.dashscope.metadata;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

public class AliyunAiDashscopeChatResponseMetadata implements ChatResponseMetadata {

    public static AliyunAiDashscopeChatResponseMetadata from(GenerationResult response) {
        Assert.notNull(response, "GenerationResult must not be null");
        AliyunAiDashscopeUsage usage = AliyunAiDashscopeUsage.from(response.getUsage());
        AliyunAiDashscopeChatResponseMetadata chatResponseMetadata = new AliyunAiDashscopeChatResponseMetadata(response.getRequestId(), usage);
        return chatResponseMetadata;
    }

    private final String id;
    private final Usage usage;

    public AliyunAiDashscopeChatResponseMetadata(String id, AliyunAiDashscopeUsage usage) {
        this.id = id;
        this.usage = usage;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public Usage getUsage() {
        Usage usage = this.usage;
        return usage != null ? usage : new EmptyUsage();
    }

}
