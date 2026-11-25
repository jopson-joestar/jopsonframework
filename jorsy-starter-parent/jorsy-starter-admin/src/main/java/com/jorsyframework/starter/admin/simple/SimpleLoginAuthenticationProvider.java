package com.jorsyframework.starter.admin.simple;

import java.util.List;
import java.util.regex.Pattern;

import com.jorsyframework.starter.admin.common.security.LoginAttempts;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

final class SimpleLoginAuthenticationProvider implements AuthenticationProvider {
    private static final String PREFIX = "{bcrypt}";
    private static final Pattern BCRYPT = Pattern.compile("[$]2[aby][$](0[4-9]|[12][0-9]|3[01])[$][./A-Za-z0-9]{53}");
    private final String username;
    private final String hash;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    private final LoginAttempts attempts = new LoginAttempts();

    SimpleLoginAuthenticationProvider(String username, String passwordHash) {
        if (username == null || username.isBlank() || username.trim().length() > 32) {
            throw new IllegalStateException("jorsy.admin.simple.username 必须为 1 至 32 个字符");
        }
        if (passwordHash == null || !passwordHash.startsWith(PREFIX)
                || !BCRYPT.matcher(passwordHash.substring(PREFIX.length())).matches()) {
            throw new IllegalStateException("jorsy.admin.simple.password-hash 必须为有效的 {bcrypt} 摘要");
        }
        this.username = username.trim();
        this.hash = passwordHash.substring(PREFIX.length());
    }

    @Override
    public Authentication authenticate(Authentication attempt) {
        attempts.check(attempt);
        String password = (String) attempt.getCredentials();
        boolean matches = LoginAttempts.acceptsPassword(password) && passwords.matches(password, hash);
        if (!matches || !username.equals(attempt.getName())) {
            throw attempts.invalid(attempt, "credentials");
        }
        return UsernamePasswordAuthenticationToken.authenticated(username, null, List.of());
    }

    @Override
    public boolean supports(Class<?> type) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type);
    }
}
