package com.geektime.ratelimiter.exception;

/**
 * @Description: 限流器内部错误异常
 * @Author: dansheng
 * @CreateTime: 2025/2/6 17:06
 **/
public class InternalErrorException extends RuntimeException {
    public InternalErrorException(String message) {
        super(message);
    }

    public InternalErrorException(String message, Throwable cause) {
        super(message, cause);
    }
} 