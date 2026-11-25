package com.jopsonframework.starter.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/** 对外使用稳定的 String 或 Integer 编码。每个枚举内的编码必须唯一。 */
public interface ValueEnum<V> {
    V getValue();

    @JsonValue
    default V toJsonValue() {
        ValueEnumCodes.validate(((Enum<?>) this).getDeclaringClass());
        return getValue();
    }
}
