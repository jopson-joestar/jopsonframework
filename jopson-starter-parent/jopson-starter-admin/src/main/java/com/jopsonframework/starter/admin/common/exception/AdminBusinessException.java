package com.jopsonframework.starter.admin.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AdminBusinessException extends RuntimeException {
    private final HttpStatus status;

    public AdminBusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static AdminBusinessException invalid(String message) {
        return new AdminBusinessException(HttpStatus.BAD_REQUEST, message);
    }

    public static AdminBusinessException conflict(String message) {
        return new AdminBusinessException(HttpStatus.CONFLICT, message);
    }

    public static AdminBusinessException missing(String message) {
        return new AdminBusinessException(HttpStatus.NOT_FOUND, message);
    }

    public static AdminBusinessException forbidden(String message) {
        return new AdminBusinessException(HttpStatus.FORBIDDEN, message);
    }

    public static <T> T requireFound(T value) {
        if (value == null) throw missing("记录不存在");
        return value;
    }

    public static void checkVersion(long actual, long expected) {
        if (actual != expected) throw conflict("版本冲突，请刷新记录");
    }

    public static void checkWrite(boolean updated, String message) {
        if (!updated) throw conflict(message);
    }
}
