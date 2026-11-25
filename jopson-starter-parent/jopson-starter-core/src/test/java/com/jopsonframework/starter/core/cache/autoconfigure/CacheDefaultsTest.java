package com.jopsonframework.starter.core.cache.autoconfigure;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CacheDefaultsTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JopsonCacheAutoConfiguration.class, CacheAutoConfiguration.class));

    @Test
    void expiresEntriesAfterThirtyMinutesWithoutSleeping() {
        AtomicLong nanos = new AtomicLong();
        Cache<Object, Object> cache = new JopsonCacheAutoConfiguration().jopsonCaffeine().ticker(nanos::get).build();
        assertThat(cache.policy().eviction().orElseThrow().getMaximum()).isEqualTo(1000);
        cache.put("key", "value");
        nanos.set(Duration.ofMinutes(30).toNanos() - 1);
        assertThat(cache.getIfPresent("key")).isEqualTo("value");
        nanos.incrementAndGet();
        assertThat(cache.getIfPresent("key")).isNull();
    }

    @Test
    void honorsBootCacheProperties() {
        context.withPropertyValues("spring.cache.cache-names=custom", "spring.cache.caffeine.spec=maximumSize=7")
                .run(app -> {
                    var manager = app.getBean(CaffeineCacheManager.class);
                    assertThat(manager.getCacheNames()).containsExactly("custom");
                    var cache = ((CaffeineCache) manager.getCache("custom")).getNativeCache();
                    assertThat(cache.policy().eviction().orElseThrow().getMaximum()).isEqualTo(7);
                });
    }

    @Test
    void usesHostCacheManager() {
        var manager = new ConcurrentMapCacheManager();
        context.withBean(CacheManager.class, () -> manager).run(app -> {
            assertThat(app).hasSingleBean(CacheManager.class);
            assertThat(app.getBean(CacheManager.class)).isSameAs(manager);
        });
    }
}
