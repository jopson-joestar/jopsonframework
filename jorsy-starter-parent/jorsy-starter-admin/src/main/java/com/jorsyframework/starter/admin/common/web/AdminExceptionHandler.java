package com.jorsyframework.starter.admin.common.web;

import com.jorsyframework.starter.admin.common.exception.AdminBusinessException;
import com.jorsyframework.starter.core.result.R;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.jorsyframework.starter.admin.modules")
@Order(0)
public class AdminExceptionHandler {
    @ExceptionHandler(AdminBusinessException.class)
    public ResponseEntity<R<Void>> rejected(AdminBusinessException ex) {
        var status = ex.getStatus();
        int code = status == HttpStatus.BAD_REQUEST ? 2001 : status.value();
        return ResponseEntity.status(status).body(new R<>(code, ex.getMessage(), null));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<R<Void>> duplicate() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new R<>(409, "有效记录的业务键已存在", null));
    }
}
