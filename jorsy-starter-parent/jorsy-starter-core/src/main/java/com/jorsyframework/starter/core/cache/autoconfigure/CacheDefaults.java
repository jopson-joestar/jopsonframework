package com.jorsyframework.starter.core.cache.autoconfigure;

import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.DefaultPropertiesPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ClassUtils;

/** 站点可能单独使用 RedisTemplate，此时仍须显式选择使用 Redis 缓存。 */
public class CacheDefaults implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.containsProperty("spring.cache.type") && ClassUtils.isPresent(
                "org.springframework.data.redis.connection.RedisConnectionFactory", getClass().getClassLoader())) {
            DefaultPropertiesPropertySource.addOrMerge(Map.of("spring.cache.type", "caffeine"), environment.getPropertySources());
        }
    }
}
