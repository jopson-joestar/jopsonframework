package com.jopsonframework.starter.core.result;

public enum ResultCode {
    SUCCESS(0, "操作成功"),
    VALIDATION_FAILED(2001, "参数校验失败"),
    INVALID_JSON(2002, "请求体格式错误"),
    BUSINESS_ERROR(3001, "业务处理失败"),
    SYSTEM_ERROR(5001, "系统异常");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
