package com.jorsyframework.starter.admin.modules.rights.access;

import com.jorsyframework.starter.admin.common.security.SessionLoginSecurity;
import com.jorsyframework.starter.admin.modules.rights.facade.ISysLoginFacade;
import com.jorsyframework.starter.admin.modules.rights.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import com.jorsyframework.starter.core.log.RequestIdFilter;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextRepository;
import tools.jackson.databind.ObjectMapper;
import java.util.List;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class SysLoginSecurityConfiguration {
    @Bean
    SecurityContextRepository loginContexts() {
        return SessionLoginSecurity.contexts();
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
                SessionLoginSecurity.reply(json, (HttpServletResponse) response, 503, "数据库暂时不可用，请稍后重试");
            }
        }, UsernamePasswordAuthenticationFilter.class);
        SessionLoginSecurity.configure(http, provider, contexts, json, (auth, context) -> {
            session.current(context.getRequest());
            return new AuthorizationDecision(true);
        });
        return http.build();
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
