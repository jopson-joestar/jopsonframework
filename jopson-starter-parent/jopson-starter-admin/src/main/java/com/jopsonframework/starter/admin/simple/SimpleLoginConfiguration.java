package com.jopsonframework.starter.admin.simple;

import com.jopsonframework.starter.admin.common.security.SessionLoginSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@Import(SimpleLoginController.class)
public class SimpleLoginConfiguration {
    @Bean
    SimpleLoginAuthenticationProvider simpleLoginAuthenticationProvider(Environment environment) {
        return new SimpleLoginAuthenticationProvider(environment.getProperty("jopson.admin.simple.username"),
                environment.getProperty("jopson.admin.simple.password-hash"));
    }

    @Bean
    SecurityFilterChain simpleLoginSecurity(HttpSecurity http, SimpleLoginAuthenticationProvider provider,
                                            ObjectMapper json) {
        SessionLoginSecurity.configure(http, provider, SessionLoginSecurity.contexts(), json,
                AuthenticatedAuthorizationManager.authenticated());
        return http.build();
    }
}
