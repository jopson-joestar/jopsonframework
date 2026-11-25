package com.jorsyframework.starter.core.result;

public record R<T>(int code, String message, T data) {

    public static <T> R<T> success(T data) {
        return new R<>(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.message(), data);
    }

    public static R<Void> failure(ResultCode error) {
        return new R<>(error.code(), error.message(), null);
    }
}
