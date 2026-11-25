package com.jorsyframework.starter.admin.modules.rights.access;

import com.jorsyframework.starter.admin.modules.rights.domain.enums.SysUserStatus;
import com.jorsyframework.starter.admin.modules.rights.facade.ISysLoginFacade;
import com.jorsyframework.starter.admin.modules.rights.service.ISysUserService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import com.jorsyframework.starter.core.log.RequestIdFilter;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Slf4j
public class SysLoginAuthenticationProvider implements AuthenticationProvider {
    public record Principal(String userId, String rightsId) implements java.io.Serializable {}
    static final String INVALID = "账号或密码错误，或账号不可用";
    static final String NO_IDENTITY = "未配置可用的默认身份，请联系管理员";
    private final ISysUserService users;
    private final ISysLoginFacade login;
    private final PasswordEncoder passwords;
    private final String dummyHash;
    private final Cache<String, Integer> failures = Caffeine.newBuilder().maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(5)).build();

    public SysLoginAuthenticationProvider(ISysUserService users, ISysLoginFacade login, PasswordEncoder passwords) {
        this.users = users;
        this.login = login;
        this.passwords = passwords;
        this.dummyHash = passwords.encode(java.util.UUID.randomUUID().toString());
    }

    @Override
    public Authentication authenticate(Authentication attempt) {
        var details = (WebAuthenticationDetails) attempt.getDetails();
        String source = details.getRemoteAddress();
        if (failures.asMap().getOrDefault(source, 0) >= 20) {
            logFailure("rate-limit");
            throw new LockedException("登录尝试过于频繁，请稍后重试");
        }
        String password = (String) attempt.getCredentials();
        var user = users.loginCandidate(attempt.getName().trim()).orElse(null);
        boolean matches = password != null && password.getBytes(StandardCharsets.UTF_8).length <= 72
                && passwords.matches(password, user == null ? dummyHash : user.getSysUserPwd());
        if (user == null || !matches || user.getSysUserStatus() != SysUserStatus.CHECK) {
            failures.asMap().merge(source, 1, Integer::sum);
            logFailure(user == null || !matches ? "credentials" : "account-state");
            throw new BadCredentialsException(INVALID);
        }
        String rightsId = login.defaultRightsId(user.getSysUserId()).orElseThrow(() -> {
            logFailure("default-identity");
            return new InsufficientAuthenticationException(NO_IDENTITY);
        });
        return authenticated(new Principal(user.getSysUserId(), rightsId));
    }

    private static void logFailure(String category) {
        log.info("登录失败 requestId={} category={}", MDC.get(RequestIdFilter.MDC_KEY), category);
    }

    static Authentication authenticated(Principal principal) {
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of());
    }

    @Override
    public boolean supports(Class<?> type) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type);
    }
}
