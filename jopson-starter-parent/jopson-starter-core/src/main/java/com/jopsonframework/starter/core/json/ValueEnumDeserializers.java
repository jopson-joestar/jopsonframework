package com.jopsonframework.starter.core.json;

import com.jopsonframework.starter.core.enums.ValueEnum;
import com.jopsonframework.starter.core.enums.ValueEnumCodes;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.exc.InvalidFormatException;

public final class ValueEnumDeserializers extends ValueDeserializerModifier {
    @Override
    public ValueDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type,
            BeanDescription.Supplier beanDescription, ValueDeserializer<?> deserializer) {
        if (!ValueEnum.class.isAssignableFrom(type.getRawClass())) {
            return deserializer;
        }
        return forType(type.getRawClass());
    }

    private static ValueDeserializer<ValueEnum<?>> forType(Class<?> type) {
        return new ValueDeserializer<>() {
            @Override
            public ValueEnum<?> deserialize(JsonParser parser, DeserializationContext context) {
                JsonToken token = parser.currentToken();
                if (token == JsonToken.VALUE_STRING || token == JsonToken.VALUE_NUMBER_INT) {
                    var value = ValueEnumCodes.find(type, parser.getString());
                    if (value != null && (value.getValue() instanceof String) == (token == JsonToken.VALUE_STRING)) {
                        return value;
                    }
                }
                parser.skipChildren();
                throw InvalidFormatException.from(parser, "未知的业务枚举编码或编码类型不匹配", null, type);
            }
        };
    }
}
