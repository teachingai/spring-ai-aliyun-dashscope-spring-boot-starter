package org.springframework.ai.aliyun.dashscope.autoconfigure;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.embeddings.TextEmbedding;
import org.springframework.ai.aliyun.dashscope.AliyunAiDashscopeChatClient;
import org.springframework.ai.aliyun.dashscope.AliyunAiDashscopeEmbeddingClient;
import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for 百度千帆 Chat Client.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@EnableConfigurationProperties({ AliyunAiDashscopeChatProperties.class, AliyunAiDashscopeConnectionProperties.class, AliyunAiDashscopeEmbeddingProperties.class })
@ConditionalOnClass(Generation.class)
public class AliyunAiDashscopeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AliyunAiDashscopeChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public AliyunAiDashscopeChatClient qianfanAiChatClient(AliyunAiDashscopeChatProperties chatProperties,
                                                           List<FunctionCallback> toolFunctionCallbacks,
                                                           FunctionCallbackContext functionCallbackContext,
                                                           ObjectProvider<RetryTemplate> retryTemplateProvider) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }
        Generation generation = new Generation();
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new AliyunAiDashscopeChatClient(generation, chatProperties.getOptions(), functionCallbackContext, retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public AliyunAiDashscopeEmbeddingClient qianfanAiEmbeddingClient(AliyunAiDashscopeEmbeddingProperties embeddingProperties) {
        TextEmbedding embedding = new TextEmbedding();
        return new AliyunAiDashscopeEmbeddingClient(embedding, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions());
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
