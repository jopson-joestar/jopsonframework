package com.jorsyframework.starter.core.openapi.autoconfigure;

import com.jorsyframework.starter.core.openapi.ValueEnumSchemaConverter;
import io.swagger.v3.core.converter.ModelConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ModelConverter.class)
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class JorsyValueEnumOpenApiConfiguration {
    @Bean
    @ConditionalOnMissingBean
    ValueEnumSchemaConverter jorsyValueEnumSchemaConverter() {
        return new ValueEnumSchemaConverter();
    }
}
