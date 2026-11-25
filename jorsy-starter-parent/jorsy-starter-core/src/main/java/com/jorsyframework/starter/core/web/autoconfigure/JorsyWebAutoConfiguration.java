package com.jorsyframework.starter.core.web.autoconfigure;

import com.jorsyframework.starter.core.web.JorsyExceptionHandler;
import com.jorsyframework.starter.core.web.ValueEnumBindingPostProcessor;
import com.jorsyframework.starter.core.web.ValueEnumConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JorsyWebAutoConfiguration {

    @Bean
    static ValueEnumBindingPostProcessor jorsyValueEnumBindingPostProcessor() {
        return new ValueEnumBindingPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    ValueEnumConverter jorsyValueEnumConverter() {
        return new ValueEnumConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    JorsyExceptionHandler jorsyExceptionHandler() {
        return new JorsyExceptionHandler();
    }
}
