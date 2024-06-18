package org.springframework.ai.aliyun.dashscope;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.aliyun.dashscope.exception.DashscopeSDKException;
import org.springframework.ai.aliyun.dashscope.util.ApiUtils;
import org.springframework.ai.chat.ChatResponse;
import reactor.core.publisher.FluxSink;

public class AliyunAiDashscopeResultCallback extends ResultCallback<GenerationResult> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private FluxSink<ChatResponse> sink;

    public AliyunAiDashscopeResultCallback(FluxSink<ChatResponse> sink) {
        this.sink = sink;
    }

    @Override
    public void onOpen(Status status) {
        log.info("ResultCallback onOpen: requestId ----> {}", status.getRequestId());
    }

    @Override
    public void onEvent(GenerationResult message) {
        log.info("ResultCallback onEvent: requestId ----> {} || output ----> {}", message.getRequestId(), message.getOutput());
        sink.next(ApiUtils.toChatCompletion(message));
    }

    @Override
    public void onComplete() {
        log.info("ResultCallback onComplete ");
        sink.complete();
    }

    @Override
    public void onError(Exception e) {
        log.error("ResultCallback onError ");
        sink.error(new DashscopeSDKException("Error occurred in stream callback with id: ", e));
    }

}
