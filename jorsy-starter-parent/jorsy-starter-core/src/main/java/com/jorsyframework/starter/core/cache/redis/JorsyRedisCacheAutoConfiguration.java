package com.jorsyframework.starter.core.cache.redis;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.BatchStrategy;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.util.Assert;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration(before = CacheAutoConfiguration.class,
        afterName = "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration")
@ConditionalOnClass(RedisCacheManager.class)
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
@ConditionalOnMissingBean(CacheManager.class)
@EnableConfigurationProperties(CacheProperties.class)
public class JorsyRedisCacheAutoConfiguration {
    @Bean
    RedisCacheManager jorsyRedisCacheManager(RedisConnectionFactory connections, CacheProperties properties,
                                              ObjectMapper mapper, List<RedisCacheSpec> specs) {
        var redis = properties.getRedis();
        Assert.hasText(redis.getKeyPrefix(), "spring.cache.redis.key-prefix 必须明确指定站点与环境前缀");
        Assert.isTrue(redis.isUseKeyPrefix(), "spring.cache.redis.use-key-prefix 必须为 true");
        var ttl = redis.getTimeToLive() == null ? Duration.ofMinutes(5) : redis.getTimeToLive();
        Assert.isTrue(ttl.compareTo(Duration.ofMillis(1)) >= 0, "spring.cache.redis.time-to-live 至少为 1ms");
        var defaults = RedisCacheConfiguration.defaultCacheConfig().prefixCacheNameWith(redis.getKeyPrefix())
                .entryTtl(ttl).disableCachingNullValues();
        Map<String, RedisCacheConfiguration> caches = new HashMap<>();
        for (var spec : specs) {
            var type = mapper.getTypeFactory().constructType(spec.valueType());
            var serializer = new JacksonJsonRedisSerializer<>(mapper, type);
            var config = defaults.serializeValuesWith(SerializationPair.fromSerializer(serializer));
            Assert.isTrue(caches.putIfAbsent(spec.name(), config) == null, "重复的 RedisCacheSpec 名称: " + spec.name());
        }
        for (String cacheName : properties.getCacheNames()) {
            Assert.isTrue(caches.containsKey(cacheName), "缓存未声明 RedisCacheSpec: " + cacheName);
        }
        // The transaction decorator determines when to evict. Wait for that Redis operation before returning.
        var writer = RedisCacheWriter.create(connections, options -> {
            options.batchStrategy(scopedScan(defaults)).immediateWrites();
            if (redis.isEnableStatistics()) {
                options.collectStatistics();
            }
        });
        return new TypedRedisCacheManager(new ResilientRedisCacheWriter(writer), defaults, caches);
    }

    private static BatchStrategy scopedScan(RedisCacheConfiguration defaults) {
        BatchStrategy scan = BatchStrategies.scan(1000);
        return (connection, name, pattern) -> {
            // SCAN MATCH interprets globs. Only the pattern after the namespace is a glob.
            String prefix = defaults.getKeyPrefixFor(name);
            String literal = prefix.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?").replace("[", "\\[");
            String scoped = new String(pattern, StandardCharsets.UTF_8);
            Assert.state(scoped.startsWith(prefix), "清除范围必须包含缓存前缀");
            return scan.cleanCache(connection, name, (literal + scoped.substring(prefix.length())).getBytes(StandardCharsets.UTF_8));
        };
    }
}
