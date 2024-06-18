package org.springframework.ai.aliyun.dashscope;

// Copyright (c) Alibaba, Inc. and its affiliates.

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class StreamCall {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static void handleGenerationResult(GenerationResult message, StringBuilder fullContent) {
        fullContent.append(message.getOutput().getChoices().get(0).getMessage().getContent());
        logger.info("Received message: {}", JsonUtils.toJson(message));
    }

    public static void streamCallWithMessage(Generation gen, Message userMsg)
            throws NoApiKeyException, ApiException, InputRequiredException {
        GenerationParam param = buildGenerationParam(userMsg);
        Flowable<GenerationResult> result = gen.streamCall(param);
        StringBuilder fullContent = new StringBuilder();

        result.blockingForEach(message -> handleGenerationResult(message, fullContent));

        logger.info("Full content: \n{}", fullContent.toString());
    }

    public static void streamCallWithCallback(Generation gen, Message userMsg)
            throws NoApiKeyException, ApiException, InputRequiredException, InterruptedException {
        GenerationParam param = buildGenerationParam(userMsg);
        Semaphore semaphore = new Semaphore(0);
        StringBuilder fullContent = new StringBuilder();

        gen.streamCall(param, new ResultCallback<GenerationResult>() {
            @Override
            public void onEvent(GenerationResult message) {
                handleGenerationResult(message, fullContent);
            }

            @Override
            public void onError(Exception err) {
                logger.error("Exception occurred: {}", err.getMessage());
                semaphore.release();
            }

            @Override
            public void onComplete() {
                logger.info("Completed");
                semaphore.release();
            }
        });

        semaphore.acquire();
        logger.info("Full content: \n{}", fullContent.toString());
    }

    private static GenerationParam buildGenerationParam(Message userMsg) {
        return GenerationParam.builder()
                .model("qwen-turbo")
                .messages(Arrays.asList(userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .incrementalOutput(true)
                .build();
    }

    public static void main(String[] args) {
        try {
            Generation gen = new Generation();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content("如何做西红柿炖牛腩？").build();

            streamCallWithMessage(gen, userMsg);
            streamCallWithCallback(gen, userMsg);
        } catch (ApiException | NoApiKeyException | InputRequiredException | InterruptedException e) {
            logger.error("An exception occurred: {}", e.getMessage());
        }
    }
}
