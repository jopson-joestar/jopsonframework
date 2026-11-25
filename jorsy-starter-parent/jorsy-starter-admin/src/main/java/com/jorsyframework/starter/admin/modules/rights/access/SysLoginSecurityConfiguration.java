package com.jorsyframework.starter.admin.modules.rights.access;

import com.jorsyframework.starter.admin.modules.rights.facade.ISysLoginFacade;
import com.jorsyframework.starter.admin.modules.rights.service.ISysUserService;
import com.jorsyframework.starter.admin.common.autoconfigure.JorsyAdminAutoConfiguration;
import com.jorsyframework.starter.core.result.R;
import jakarta.servlet.DispatcherType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import com.jorsyframework.starter.core.log.RequestIdFilter;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;

@Slf4j
@AutoConfiguration(before = JorsyAdminAutoConfiguration.class, beforeName = {
        "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration"})
@ConditionalOnProperty(name = "jorsy.admin.login.enabled", havingValue = "true")
@ConditionalOnClass(name = "org.springframework.security.config.annotation.web.builders.HttpSecurity")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SysLoginSecurityConfiguration {
    @Bean
    SecurityContextRepository loginContexts() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SysLoginSession sysLoginSession(ISysLoginFacade login, SecurityContextRepository contexts) {
        return new SysLoginSession(login, contexts);
    }

    @Bean
    SysLoginAuthenticationProvider sysLoginAuthenticationProvider(ISysUserService users, ISysLoginFacade login,
                                                                 PasswordEncoder encoder) {
        return new SysLoginAuthenticationProvider(users, login, encoder);
    }

    @Bean
    SecurityFilterChain loginSecurity(HttpSecurity http, SysLoginSession session, ObjectMapper json,
                                      SysLoginAuthenticationProvider provider, SecurityContextRepository contexts) {
        http.addFilterBefore((request, response, chain) -> {
            try {
                chain.doFilter(request, response);
            } catch (DataAccessException | TransactionException failure) {
                log.error("后台数据访问失败 requestId={} category={}", MDC.get(RequestIdFilter.MDC_KEY),
                        failure.getClass().getSimpleName());
                if (response.isCommitted()) throw failure;
                reply(json, (HttpServletResponse) response, 503, "数据库暂时不可用，请稍后重试");
            }
        }, UsernamePasswordAuthenticationFilter.class);
        http.authenticationManager(new ProviderManager(provider))
                .securityContext(config -> config.securityContextRepository(contexts))
                .requestCache(config -> config.disable())
                .authorizeHttpRequests(config -> config.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/auth/csrf", "/auth/login", "/actuator/health").permitAll()
                        .anyRequest().access((auth, context) -> {
                            session.current(context.getRequest());
                            return new AuthorizationDecision(true);
                        }))
                .exceptionHandling(config -> config
                        .authenticationEntryPoint((req, res, ex) -> reply(json, res, 401, "请先登录"))
                        .accessDeniedHandler((req, res, ex) -> {
                            boolean loginRequest = req.getServletPath().equals("/auth/login");
                            var auth = SecurityContextHolder.getContext().getAuthentication();
                            boolean anonymous = auth == null || !(auth.getPrincipal() instanceof SysLoginAuthenticationProvider.Principal);
                            if (!loginRequest && anonymous) reply(json, res, 401, "请先登录");
                            else reply(json, res, 403, "请求验证失败，请刷新页面");
                        }))
                .formLogin(config -> config.loginPage("/auth/login").loginProcessingUrl("/auth/login")
                        .successHandler((req, res, auth) -> reply(json, res, 200, null))
                        .failureHandler((req, res, error) -> {
                            if (error instanceof LockedException) reply(json, res, 429, error.getMessage());
                            else if (error instanceof InsufficientAuthenticationException)
                                reply(json, res, 403, SysLoginAuthenticationProvider.NO_IDENTITY);
                            else if (error instanceof BadCredentialsException)
                                reply(json, res, 401, SysLoginAuthenticationProvider.INVALID);
                            else reply(json, res, 500, "登录服务暂时不可用");
                        }))
                .logout(config -> config.logoutUrl("/auth/logout").deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((req, res, auth) -> reply(json, res, 200, null)));
        return http.build();
    }

    private static void reply(ObjectMapper json, HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        json.writeValue(response.getOutputStream(), status == 200 ? R.success(null) : new R<>(status, message, null));
    }

    @Bean
    ApplicationRunner verifyLoginSchema(JdbcTemplate jdbc) {
        return args -> {
            for (String table : List.of("sys_user", "sys_org", "sys_role", "sys_rights", "sys_rights_extra",
                    "sys_res", "sys_role_auth", "sys_config", "sys_serial", "sys_dict", "sys_dict_item")) {
                jdbc.queryForList("SELECT 1 FROM " + table + " WHERE 1=0");
            }
        };
    }
}
