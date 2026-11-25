package com.jorsyframework.starter.core.exception;

public class BusinessException extends RuntimeException {

    /** message 是可以公开给调用方的业务说明。 */
    public BusinessException(String message) {
        super(message);
    }
}
