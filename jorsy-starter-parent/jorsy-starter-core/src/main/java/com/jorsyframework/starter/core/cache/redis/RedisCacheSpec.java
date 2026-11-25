package com.jorsyframework.starter.core.cache.redis;

import java.lang.reflect.Type;
import java.util.Objects;

import org.springframework.util.Assert;

/** 一个缓存名对应一种值类型。泛型值可通过 ParameterizedTypeReference.getType() 声明。 */
public record RedisCacheSpec(String name, Type valueType) {
    public RedisCacheSpec {
        Assert.hasText(name, "RedisCacheSpec.name 必须包含缓存名");
        Objects.requireNonNull(valueType, "RedisCacheSpec.valueType");
    }
}
