package com.jopsonframework.starter.core.log;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

public class RequestIdTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable task) {
        String captured = MDC.get(RequestIdFilter.MDC_KEY);
        return () -> {
            String previous = MDC.get(RequestIdFilter.MDC_KEY);
            try {
                RequestIdFilter.restoreRequestId(captured);
                task.run();
            } finally {
                RequestIdFilter.restoreRequestId(previous);
            }
        };
    }
}
