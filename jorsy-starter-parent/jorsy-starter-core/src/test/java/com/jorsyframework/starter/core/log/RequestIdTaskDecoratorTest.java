package com.jorsyframework.starter.core.log;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestIdTaskDecoratorTest {

    @Test
    void restoresWorkerIdEvenWhenTaskFails() {
        String key = RequestIdFilter.MDC_KEY;
        try {
            MDC.put(key, "request");
            Runnable decorated = new RequestIdTaskDecorator().decorate(() -> {
                assertThat(MDC.get(key)).isEqualTo("request");
                throw new IllegalStateException("test");
            });
            MDC.put(key, "worker");
            assertThatThrownBy(decorated::run).isInstanceOf(IllegalStateException.class);
            assertThat(MDC.get(key)).isEqualTo("worker");
        } finally {
            MDC.remove(key);
        }
    }
}
