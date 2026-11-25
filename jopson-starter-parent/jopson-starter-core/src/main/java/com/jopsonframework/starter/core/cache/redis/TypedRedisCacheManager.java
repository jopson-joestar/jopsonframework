package com.jopsonframework.starter.core.cache.redis;

import java.util.Map;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

final class TypedRedisCacheManager extends RedisCacheManager {
    private final Map<String, RedisCacheConfiguration> configurations;

    TypedRedisCacheManager(RedisCacheWriter writer, RedisCacheConfiguration defaults,
                           Map<String, RedisCacheConfiguration> configurations) {
        super(writer, defaults, false, configurations);
        this.configurations = Map.copyOf(configurations);
        setTransactionAware(true);
    }

    @Override
    public Map<String, RedisCacheConfiguration> getCacheConfigurations() {
        // Spring Data Redis 4.2 casts the exposed transaction decorator to RedisCache here.
        return configurations;
    }
}
