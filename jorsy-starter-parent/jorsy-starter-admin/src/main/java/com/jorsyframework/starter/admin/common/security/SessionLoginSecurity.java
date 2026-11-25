package com.jorsyframework.starter.admin.common.security;

import java.io.IOException;

import com.jorsyframework.starter.core.result.R;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import tools.jackson.databind.ObjectMapper;

public final class SessionLoginSecurity {
    public static final String INVALID_CREDENTIALS = "账号或密码错误，或账号不可用";

    private SessionLoginSecurity() {
    }

    public static void configure(HttpSecurity http, AuthenticationProvider provider,
                                 SecurityContextRepository contexts, ObjectMapper json,
                                 AuthorizationManager<RequestAuthorizationContext> authorization) {
        http.authenticationManager(new ProviderManager(provider))
                .securityContext(config -> config.securityContextRepository(contexts))
                .requestCache(config -> config.disable())
                .authorizeHttpRequests(config -> config.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/auth/csrf", "/auth/login", "/actuator/health").permitAll()
                        .anyRequest().access(authorization))
                .exceptionHandling(config -> config
                        .authenticationEntryPoint((req, res, ex) -> reply(json, res, 401, "请先登录"))
                        .accessDeniedHandler((req, res, ex) -> {
                            boolean loginRequest = req.getServletPath().equals("/auth/login");
                            var auth = SecurityContextHolder.getContext().getAuthentication();
                            boolean anonymous = auth == null || !auth.isAuthenticated()
                                    || auth instanceof AnonymousAuthenticationToken;
                            if (!loginRequest && anonymous) reply(json, res, 401, "请先登录");
                            else reply(json, res, 403, "请求验证失败，请刷新页面");
                        }))
                .formLogin(config -> config.loginPage("/auth/login").loginProcessingUrl("/auth/login")
                        .successHandler((req, res, auth) -> reply(json, res, 200, null))
                        .failureHandler((req, res, error) -> {
                            if (error instanceof LockedException) reply(json, res, 429, error.getMessage());
                            else if (error instanceof InsufficientAuthenticationException)
                                reply(json, res, 403, error.getMessage());
                            else if (error instanceof BadCredentialsException)
                                reply(json, res, 401, INVALID_CREDENTIALS);
                            else reply(json, res, 500, "登录服务暂时不可用");
                        }))
                .logout(config -> config.logoutUrl("/auth/logout").deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((req, res, auth) -> reply(json, res, 200, null)));
    }

    public static SecurityContextRepository contexts() {
        return new HttpSessionSecurityContextRepository();
    }

    public static void reply(ObjectMapper json, HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        json.writeValue(response.getOutputStream(), status == 200 ? R.success(null) : new R<>(status, message, null));
    }
}
