package com.jorsyframework.starter.core.json.autoconfigure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jorsyframework.starter.core.json.ValueEnumDeserializers;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.module.SimpleModule;

@AutoConfiguration
public class JorsyJsonAutoConfiguration {

    @Bean
    @Order(-1)
    JsonMapperBuilderCustomizer jorsyJsonDefaults() {
        SimpleModule module = new SimpleModule("jorsy-business-types");
        module.setDeserializerModifier(new ValueEnumDeserializers());
        module.addSerializer(BigDecimal.class, new ValueSerializer<>() {
            @Override
            public void serialize(BigDecimal value, JsonGenerator generator, SerializationContext context) {
                generator.writeString(value.stripTrailingZeros().toPlainString());
            }
        });
        return builder -> builder.addModule(module)
                .withConfigOverride(LocalDate.class, type -> type.setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd")))
                .withConfigOverride(LocalDateTime.class,
                        type -> type.setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd HH:mm:ss")))
                .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
                        DateTimeFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE);
    }
}
