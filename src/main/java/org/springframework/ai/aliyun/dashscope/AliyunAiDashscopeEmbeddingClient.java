package org.springframework.ai.aliyun.dashscope;

import com.alibaba.dashscope.embeddings.*;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.*;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class AliyunAiDashscopeEmbeddingClient extends AbstractEmbeddingClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AliyunAiDashscopeEmbeddingOptions defaultOptions;

    private final MetadataMode metadataMode;

    private final TextEmbedding embedding;

    public AliyunAiDashscopeEmbeddingClient(TextEmbedding embedding) {
        this(embedding, MetadataMode.EMBED);
    }

    public AliyunAiDashscopeEmbeddingClient(TextEmbedding embedding, MetadataMode metadataMode) {
        this(embedding, metadataMode, AliyunAiDashscopeEmbeddingOptions.builder().build());
    }

    public AliyunAiDashscopeEmbeddingClient(TextEmbedding embedding, MetadataMode metadataMode, AliyunAiDashscopeEmbeddingOptions options) {
        Assert.notNull(embedding, "TextEmbedding must not be null");
        Assert.notNull(metadataMode, "metadataMode must not be null");
        Assert.notNull(options, "options must not be null");

        this.embedding = embedding;
        this.metadataMode = metadataMode;
        this.defaultOptions = options;
    }

    @Override
    public List<Double> embed(Document document) {
        logger.debug("Retrieving embeddings");
        EmbeddingResponse response = this.call(new EmbeddingRequest(List.of(document.getFormattedContent(this.metadataMode)), null));
        logger.debug("Embeddings retrieved");
        return response.getResults().stream().map(embedding -> embedding.getOutput()).flatMap(List::stream).toList();
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {

        logger.debug("Retrieving embeddings");

        TextEmbeddingParam embeddingRequest = this.toEmbeddingRequest(request, TextEmbeddingParam.TextType.DOCUMENT);
        TextEmbeddingResult embeddingResponse = null;
        try {
            embeddingResponse = embedding.call(embeddingRequest);
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        }
        if (embeddingResponse == null) {
            logger.warn("No embeddings returned for request: {}", request);
            return new EmbeddingResponse(List.of());
        }

        logger.debug("Embeddings retrieved");
        return generateEmbeddingResponse(embeddingRequest.getModel(), embeddingResponse);
    }

    TextEmbeddingParam toEmbeddingRequest(EmbeddingRequest embeddingRequest, TextEmbeddingParam.TextType textType) {
        if (embeddingRequest.getOptions() != null && !EmbeddingOptions.EMPTY.equals(embeddingRequest.getOptions())) {
            if (embeddingRequest.getOptions() instanceof AliyunAiDashscopeEmbeddingOptions embeddingOptions) {
               return TextEmbeddingParam.builder()
                        .textType(textType)
                        .model(embeddingOptions.getModel())
                        .texts(embeddingRequest.getInstructions())
                        .build();
            }
        }
        else if (this.defaultOptions != null) {
            return TextEmbeddingParam.builder()
                    .textType(textType)
                    .model(this.defaultOptions.getModel())
                    .texts(embeddingRequest.getInstructions())
                    .build();
        }
        return TextEmbeddingParam.builder()
                .textType(textType)
                .texts(embeddingRequest.getInstructions())
                .build();
    }

    private EmbeddingResponse generateEmbeddingResponse(String model, TextEmbeddingResult embeddingResponse) {
        List<Embedding> data = generateEmbeddingList(embeddingResponse.getOutput().getEmbeddings());
        EmbeddingResponseMetadata metadata = generateMetadata(model, embeddingResponse.getUsage());
        return new EmbeddingResponse(data, metadata);
    }

    private List<Embedding> generateEmbeddingList(List<TextEmbeddingResultItem> nativeData) {
        List<Embedding> data = new ArrayList<>();
        for (TextEmbeddingResultItem nativeDatum : nativeData) {
            Embedding embedding = new Embedding(nativeDatum.getEmbedding(), nativeDatum.getTextIndex());
            data.add(embedding);
        }
        return data;
    }

    private EmbeddingResponseMetadata generateMetadata(String model, TextEmbeddingUsage embeddingsUsage) {
        EmbeddingResponseMetadata metadata = new EmbeddingResponseMetadata();
        metadata.put("model", model);
        metadata.put("total-tokens", embeddingsUsage.getTotalTokens());
        return metadata;
    }

}
