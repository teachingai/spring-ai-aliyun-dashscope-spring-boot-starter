package org.springframework.ai.aliyun.dashscope.exception;

public class DashscopeSDKException extends RuntimeException {
    public DashscopeSDKException() {
    }

    public DashscopeSDKException(String message) {
        super(message);
    }

    public DashscopeSDKException(String message, Throwable cause) {
        super(message, cause);
    }

    public DashscopeSDKException(Throwable cause) {
        super(cause);
    }
}
