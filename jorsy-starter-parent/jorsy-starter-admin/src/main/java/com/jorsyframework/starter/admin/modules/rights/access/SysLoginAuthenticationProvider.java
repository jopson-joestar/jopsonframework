package com.jorsyframework.starter.admin.modules.rights.access;

import com.jorsyframework.starter.admin.common.security.LoginAttempts;
import com.jorsyframework.starter.admin.modules.rights.domain.enums.SysUserStatus;
import com.jorsyframework.starter.admin.modules.rights.facade.ISysLoginFacade;
import com.jorsyframework.starter.admin.modules.rights.service.ISysUserService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

public class SysLoginAuthenticationProvider implements AuthenticationProvider {
    public record Principal(String userId, String rightsId) implements java.io.Serializable {}
    static final String NO_IDENTITY = "未配置可用的默认身份，请联系管理员";
    private final ISysUserService users;
    private final ISysLoginFacade login;
    private final PasswordEncoder passwords;
    private final String dummyHash;
    private final LoginAttempts attempts = new LoginAttempts();

    public SysLoginAuthenticationProvider(ISysUserService users, ISysLoginFacade login, PasswordEncoder passwords) {
        this.users = users;
        this.login = login;
        this.passwords = passwords;
        this.dummyHash = passwords.encode(java.util.UUID.randomUUID().toString());
    }

    @Override
    public Authentication authenticate(Authentication attempt) {
        attempts.check(attempt);
        String password = (String) attempt.getCredentials();
        var user = users.loginCandidate(attempt.getName().trim()).orElse(null);
        boolean matches = LoginAttempts.acceptsPassword(password)
                && passwords.matches(password, user == null ? dummyHash : user.getSysUserPwd());
        if (user == null || !matches || user.getSysUserStatus() != SysUserStatus.CHECK) {
            throw attempts.invalid(attempt, user == null || !matches ? "credentials" : "account-state");
        }
        String rightsId = login.defaultRightsId(user.getSysUserId()).orElseThrow(() -> {
            LoginAttempts.logFailure("default-identity");
            return new InsufficientAuthenticationException(NO_IDENTITY);
        });
        return authenticated(new Principal(user.getSysUserId(), rightsId));
    }

    static Authentication authenticated(Principal principal) {
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of());
    }

    @Override
    public boolean supports(Class<?> type) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type);
    }
}
