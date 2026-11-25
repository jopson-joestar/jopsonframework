package com.jorsyframework.starter.core.cache.autoconfigure;

import java.time.Duration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = CacheAutoConfiguration.class)
@EnableCaching
public class JorsyCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean({Caffeine.class, CaffeineSpec.class, CacheManager.class})
    Caffeine<Object, Object> jorsyCaffeine() {
        return Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofMinutes(30));
    }
}
