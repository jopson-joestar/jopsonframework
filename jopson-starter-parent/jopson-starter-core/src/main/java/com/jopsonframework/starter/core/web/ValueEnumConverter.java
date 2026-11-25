package com.jopsonframework.starter.core.web;

import java.util.Set;

import com.jopsonframework.starter.core.enums.ValueEnum;
import com.jopsonframework.starter.core.enums.ValueEnumCodes;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

public final class ValueEnumConverter implements ConditionalGenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(String.class, Enum.class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return ValueEnum.class.isAssignableFrom(targetType.getType());
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null || ((String) source).isBlank()) {
            return null;
        }
        var value = ValueEnumCodes.find(targetType.getType(), (String) source);
        if (value == null) {
            throw new IllegalArgumentException("未知的业务枚举编码");
        }
        return value;
    }
}
