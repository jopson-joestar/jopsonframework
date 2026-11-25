package com.jopsonframework.starter.admin.common.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jopsonframework.starter.core.log.RequestIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Slf4j
public final class LoginAttempts {
    private final Cache<String, Integer> failures = Caffeine.newBuilder().maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(5)).build();

    public void check(Authentication attempt) {
        if (failures.asMap().getOrDefault(source(attempt), 0) >= 20) {
            logFailure("rate-limit");
            throw new LockedException("登录尝试过于频繁，请稍后重试");
        }
    }

    public BadCredentialsException invalid(Authentication attempt, String category) {
        failures.asMap().merge(source(attempt), 1, Integer::sum);
        logFailure(category);
        return new BadCredentialsException(SessionLoginSecurity.INVALID_CREDENTIALS);
    }

    public static boolean acceptsPassword(String password) {
        return password != null && password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    public static void logFailure(String category) {
        log.info("登录失败 requestId={} category={}", MDC.get(RequestIdFilter.MDC_KEY), category);
    }

    private static String source(Authentication attempt) {
        return ((WebAuthenticationDetails) attempt.getDetails()).getRemoteAddress();
    }
}
