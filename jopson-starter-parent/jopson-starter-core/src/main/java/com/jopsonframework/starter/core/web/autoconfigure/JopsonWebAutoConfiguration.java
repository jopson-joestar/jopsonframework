package com.jopsonframework.starter.core.web.autoconfigure;

import com.jopsonframework.starter.core.web.JopsonExceptionHandler;
import com.jopsonframework.starter.core.web.ValueEnumBindingPostProcessor;
import com.jopsonframework.starter.core.web.ValueEnumConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JopsonWebAutoConfiguration {

    @Bean
    static ValueEnumBindingPostProcessor jopsonValueEnumBindingPostProcessor() {
        return new ValueEnumBindingPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    ValueEnumConverter jopsonValueEnumConverter() {
        return new ValueEnumConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    JopsonExceptionHandler jopsonExceptionHandler() {
        return new JopsonExceptionHandler();
    }
}
