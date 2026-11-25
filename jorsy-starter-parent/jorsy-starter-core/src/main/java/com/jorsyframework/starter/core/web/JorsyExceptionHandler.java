package com.jorsyframework.starter.core.web;

import com.jorsyframework.starter.core.enums.ValueEnum;
import com.jorsyframework.starter.core.exception.BusinessException;
import com.jorsyframework.starter.core.result.R;
import com.jorsyframework.starter.core.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class JorsyExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(JorsyExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                              HttpStatusCode status, WebRequest request) {
        R<Void> response;
        if (ex instanceof HttpMessageNotReadableException unreadable) {
            boolean invalidCode = unreadable.getMostSpecificCause() instanceof InvalidFormatException format
                    && format.getTargetType() != null && ValueEnum.class.isAssignableFrom(format.getTargetType());
            response = R.failure(invalidCode ? ResultCode.VALIDATION_FAILED : ResultCode.INVALID_JSON);
        } else if (status.value() == 400) {
            response = R.failure(ResultCode.VALIDATION_FAILED);
        } else if (status.is5xxServerError()) {
            LOG.error("MVC 请求处理失败", ex);
            response = R.failure(ResultCode.SYSTEM_ERROR);
        } else {
            response = protocolError(status);
        }
        return super.handleExceptionInternal(ex, response, headers, status, request);
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<R<Void>> businessError(BusinessException ex) {
        return ResponseEntity.badRequest().body(new R<>(ResultCode.BUSINESS_ERROR.code(), ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<R<Void>> unexpectedError(Exception ex) {
        ResponseStatus declared = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        if (declared != null && !declared.code().is5xxServerError()) {
            return ResponseEntity.status(declared.code()).body(protocolError(declared.code()));
        }
        LOG.error("请求处理失败", ex);
        HttpStatus status = declared == null ? HttpStatus.INTERNAL_SERVER_ERROR : declared.code();
        return ResponseEntity.status(status).body(R.failure(ResultCode.SYSTEM_ERROR));
    }

    private R<Void> protocolError(HttpStatusCode status) {
        String message = switch (status.value()) {
            case 404 -> "请求路径不存在";
            case 405 -> "不支持的请求方法";
            case 415 -> "不支持的请求媒体类型";
            default -> "请求无法处理";
        };
        return new R<>(status.value(), message, null);
    }
}
