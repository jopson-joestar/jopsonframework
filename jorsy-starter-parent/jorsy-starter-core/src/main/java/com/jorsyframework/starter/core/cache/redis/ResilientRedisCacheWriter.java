package com.jorsyframework.starter.core.cache.redis;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Supplier;

import com.jorsyframework.starter.core.log.RequestIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;

/** 位于事务装饰器之下，只处理实际 Redis I/O。业务查询和 JSON 转换在故障处理范围之外。 */
final class ResilientRedisCacheWriter implements RedisCacheWriter {
    private static final Logger LOG = LoggerFactory.getLogger(ResilientRedisCacheWriter.class);
    private final RedisCacheWriter delegate;

    ResilientRedisCacheWriter(RedisCacheWriter delegate) {
        this.delegate = delegate;
    }

    @Override
    public byte[] get(String name, byte[] key) {
        return attempt(name, "get", () -> delegate.get(name, key), null);
    }

    @Override
    public void put(String name, byte[] key, byte[] value, Duration ttl) {
        run(name, "put", () -> delegate.put(name, key, value, ttl));
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
        return attempt(name, "putIfAbsent", () -> delegate.putIfAbsent(name, key, value, ttl), null);
    }

    @Override
    public void evict(String name, byte[] key) {
        run(name, "evict", () -> delegate.evict(name, key));
    }

    @Override
    public boolean evictIfPresent(String name, byte[] key) {
        return attempt(name, "evictIfPresent", () -> delegate.evictIfPresent(name, key), false);
    }

    @Override
    public void clear(String name, byte[] pattern) {
        run(name, "clear", () -> delegate.clear(name, pattern));
    }

    @Override
    public boolean invalidate(String name, byte[] pattern) {
        return attempt(name, "invalidate", () -> delegate.invalidate(name, pattern), false);
    }

    private static void run(String name, String operation, Runnable action) {
        attempt(name, operation, () -> {
            action.run();
            return null;
        }, null);
    }

    private static <T> T attempt(String name, String operation, Supplier<T> action, T fallback) {
        try {
            return action.get();
        } catch (RuntimeException error) {
            if (!unavailable(error)) {
                throw error;
            }
            warn(name, operation, MDC.get(RequestIdFilter.MDC_KEY));
            return fallback;
        }
    }

    private static boolean unavailable(Throwable error) {
        // Lettuce may wrap a server-rejected handshake (e.g. WRONGPASS) in a connection exception.
        if (error instanceof RedisConnectionFailureException && error.getCause() != null) {
            for (Throwable cause = error.getCause(); cause != null; cause = cause.getCause()) {
                if (cause.getClass().getName().equals("io.lettuce.core.RedisCommandExecutionException")
                        || cause instanceof javax.net.ssl.SSLException) {
                    return false;
                }
            }
        }
        return error instanceof RedisConnectionFailureException || error instanceof QueryTimeoutException;
    }

    private static void warn(String name, String operation, String requestId) {
        LOG.warn("Redis 缓存操作失败 cache={} operation={} requestId={}", name, operation, requestId);
    }

    @Override
    public boolean supportsAsyncRetrieve() {
        return delegate.supportsAsyncRetrieve();
    }

    @Override
    public CompletableFuture<byte[]> retrieve(String name, byte[] key, Duration ttl) {
        return async(name, "get", () -> delegate.retrieve(name, key, ttl));
    }

    @Override
    public CompletableFuture<Void> store(String name, byte[] key, byte[] value, Duration ttl) {
        return async(name, "put", () -> delegate.store(name, key, value, ttl));
    }

    private static <T> CompletableFuture<T> async(String name, String operation,
                                                  Supplier<CompletableFuture<T>> action) {
        String requestId = MDC.get(RequestIdFilter.MDC_KEY);
        return attempt(name, operation, action, CompletableFuture.completedFuture(null)).exceptionally(error -> {
            Throwable cause = error instanceof CompletionException ? error.getCause() : error;
            if (!unavailable(cause)) {
                throw new CompletionException(cause);
            }
            warn(name, operation, requestId);
            return null;
        });
    }

    @Override
    public <T> T execute(Function<RedisConnection, T> callback) {
        return delegate.execute(callback);
    }

    @Override
    public CacheStatistics getCacheStatistics(String name) {
        return delegate.getCacheStatistics(name);
    }

    @Override
    public void clearStatistics(String name) {
        delegate.clearStatistics(name);
    }

    @Override
    public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector collector) {
        return new ResilientRedisCacheWriter(delegate.withStatisticsCollector(collector));
    }
}
