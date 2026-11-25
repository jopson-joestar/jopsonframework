package com.jopsonframework.starter.core.mybatis.log;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class JopsonSqlFormatTest {
    @ParameterizedTest
    @ValueSource(strings = {"SELECT 'secret', ?", "SELECT ? -- secret", "SELECT ? # secret", "SELECT ? /*secret*/",
            "SELECT ?; SELECT secret", "SELECT ? + 42", "SELECT ?, 0xC0DE", "SELECT ?, TRUE", "CALL secret(?)"})
    void fallsBackToSummary(String template) {
        String message = new JopsonSqlFormat().formatMessage(1, "", 7, "statement", template, "expanded secret", "jdbc:secret");
        assertThat(message).matches("requestId=- sqlType=(SELECT|OTHER) elapsedMs=7");
    }
}
