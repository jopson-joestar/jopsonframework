package com.jopsonframework.starter.core.cache.redis;

import java.time.Duration;

import com.jopsonframework.starter.core.cache.autoconfigure.JopsonCacheAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisCacheConfigurationTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JopsonRedisCacheAutoConfiguration.class))
            .withBean(ObjectMapper.class, () -> JsonMapper.builder().build())
            .withBean(RedisConnectionFactory.class, () -> mock(RedisConnectionFactory.class))
            .withBean("detail", RedisCacheSpec.class, () -> new RedisCacheSpec("detail", String.class));

    @Test
    void explicitlySelectsRedisWithTypedNamedCaches() {
        context.run(app -> assertThat(app).doesNotHaveBean(RedisCacheManager.class));
        context.withPropertyValues("spring.cache.type=redis", "spring.cache.redis.key-prefix=orders:prod:")
                .run(app -> {
                    var manager = app.getBean(RedisCacheManager.class);
                    var config = manager.getCacheConfigurations().get("detail");
                    assertThat(manager.isTransactionAware()).isTrue();
                    assertThat(manager.getCache("undeclared")).isNull();
                    assertThat(config.getKeyPrefixFor("detail")).isEqualTo("orders:prod:detail::");
                    assertThat(config.getTtlFunction().getTimeToLive("42", "value")).isEqualTo(Duration.ofMinutes(5));
                    assertThat(config.isTimeToIdleEnabled()).isFalse();
                });
    }

    @Test
    void hostManagerTakesPrecedenceEvenWithoutRedisSettings() {
        var own = new ConcurrentMapCacheManager();
        context.withBean(CacheManager.class, () -> own).withPropertyValues("spring.cache.type=redis")
                .run(app -> assertThat(app.getBean(CacheManager.class)).isSameAs(own));
    }

    @Test
    void havingARedisConnectionDoesNotSelectRedisCaching() {
        try (var site = new SpringApplicationBuilder(RedisCapableSite.class).web(WebApplicationType.NONE).run()) {
            assertThat(site.getBean(RedisConnectionFactory.class)).isNotNull();
            assertThat(site.getBean(CacheManager.class)).isInstanceOf(CaffeineCacheManager.class);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration({JopsonCacheAutoConfiguration.class, JopsonRedisCacheAutoConfiguration.class, CacheAutoConfiguration.class})
    static class RedisCapableSite {
        @Bean
        RedisConnectionFactory connections() {
            return mock(RedisConnectionFactory.class);
        }
    }

    @Test
    void annotatedMethodReportsUndeclaredCacheName() {
        context.withConfiguration(AutoConfigurations.of(JopsonCacheAutoConfiguration.class))
                .withBean(MissingCacheLookup.class).withPropertyValues("spring.cache.type=redis",
                        "spring.cache.redis.key-prefix=orders:prod:").run(app ->
                        assertThatThrownBy(() -> app.getBean(MissingCacheLookup.class).read())
                                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("undeclared"));
    }

    static class MissingCacheLookup {
        @Cacheable("undeclared")
        public String read() { throw new AssertionError("未注册的缓存应在业务查询前报错"); }
    }

    @ParameterizedTest
    @ValueSource(strings = {"key-prefix=", "use-key-prefix=false", "time-to-live=0", "time-to-live=-1s"})
    void rejectsInvalidSettings(String setting) {
        context.withPropertyValues("spring.cache.type=redis", "spring.cache.redis.key-prefix=site:test:",
                        "spring.cache.redis." + setting)
                .run(app -> assertThat(app.getStartupFailure()).hasStackTraceContaining("spring.cache.redis."
                        + setting.substring(0, setting.indexOf('='))));
    }

    @Test
    void rejectsDuplicateNames() {
        context.withPropertyValues("spring.cache.type=redis", "spring.cache.redis.key-prefix=test:")
                .withBean("other", RedisCacheSpec.class, () -> new RedisCacheSpec("detail", Integer.class))
                .run(app -> assertThat(app.getStartupFailure()).hasStackTraceContaining("重复的 RedisCacheSpec 名称: detail"));
    }

    @Test
    void treatsTimeoutAsAMissAndPropagatesAccessErrors() {
        var delegate = mock(RedisCacheWriter.class);
        var writer = new ResilientRedisCacheWriter(delegate);
        byte[] key = {42};
        var denied = new PermissionDeniedDataAccessException("Redis ACL denied access", null);
        when(delegate.get("detail", key)).thenThrow(new QueryTimeoutException("timeout")).thenThrow(denied);
        assertThat(writer.get("detail", key)).isNull();
        assertThatThrownBy(() -> writer.get("detail", key)).isSameAs(denied);
    }
}
