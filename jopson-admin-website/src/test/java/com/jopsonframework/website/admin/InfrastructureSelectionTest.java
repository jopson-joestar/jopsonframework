package com.jopsonframework.website.admin;

import javax.sql.DataSource;

import com.jopsonframework.starter.admin.modules.rights.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;

class InfrastructureSelectionTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(AdminWebsiteApplication.class);

    @Test
    void enablesBusinessDatabaseWithAdminDisabled() {
        runner.withPropertyValues("jopson.admin.enabled=false", "jopson.datasource.enabled=true",
                        "spring.datasource.url=jdbc:mysql://127.0.0.1:1/example", "spring.sql.init.mode=never",
                        "spring.cache.type=none")
                .run(context -> {
                    assertThat(context).hasSingleBean(DataSource.class).hasSingleBean(NoOpCacheManager.class)
                            .doesNotHaveBean(SysUserMapper.class).doesNotHaveBean(RedisConnectionFactory.class);
                });
    }

    @Test
    void usesEhcacheWithoutRedisOrDatabase() {
        runner.withPropertyValues("jopson.admin.enabled=false", "spring.cache.type=jcache",
                        "spring.cache.cache-names=probe")
                .run(context -> {
                    assertThat(context).hasSingleBean(JCacheCacheManager.class)
                            .doesNotHaveBean(DataSource.class).doesNotHaveBean(RedisConnectionFactory.class);
                    var cache = context.getBean(JCacheCacheManager.class).getCache("probe");
                    assertThat(cache).isNotNull();
                    cache.put("key", "value");
                    assertThat(cache.get("key", String.class)).isEqualTo("value");
                    cache.evict("key");
                    assertThat(cache.get("key")).isNull();
                });
    }

    @Test
    void enablesRedisConnectionOnlyForRedisCache() {
        runner.withPropertyValues("jopson.admin.enabled=false", "spring.cache.type=redis",
                        "spring.cache.redis.key-prefix=jopson:test:", "spring.data.redis.port=1")
                .run(context -> assertThat(context).hasSingleBean(RedisCacheManager.class)
                        .hasSingleBean(RedisConnectionFactory.class).doesNotHaveBean(DataSource.class));
    }

    @Test
    void rejectsDisabledDatabaseWhenAdminIsFull() {
        runner.withPropertyValues("jopson.datasource.enabled=false")
                .run(context -> assertThat(context).hasFailed().getFailure()
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("full 后台要求 jopson.datasource.enabled=true"));
    }
}
