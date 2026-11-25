package com.jorsyframework.website.admin.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnResource(resources = "classpath:/static/admin-web/index.html")
public class EmbeddedAdminWebConfiguration implements WebMvcConfigurer {
    private final boolean enabled;

    public EmbeddedAdminWebConfiguration(@Value("${jorsy.admin.enabled:true}") boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        if (!enabled) {
            registry.addStatusController("/admin-web", HttpStatus.NOT_FOUND);
            registry.addStatusController("/admin-web/**", HttpStatus.NOT_FOUND);
            return;
        }
        registry.addRedirectViewController("/", "/admin-web/");
        registry.addRedirectViewController("/admin-web", "/admin-web/");
        registry.addViewController("/admin-web/").setViewName("forward:/admin-web/index.html");
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnProperty(prefix = "jorsy.admin", name = "enabled", havingValue = "true", matchIfMissing = true)
    SecurityFilterChain embeddedAdminWebSecurity(HttpSecurity http) {
        var paths = PathPatternRequestMatcher.withDefaults();
        return http.securityMatcher(new OrRequestMatcher(
                        paths.matcher(HttpMethod.GET, "/"),
                        paths.matcher(HttpMethod.HEAD, "/"),
                        paths.matcher(HttpMethod.GET, "/admin-web/**"),
                        paths.matcher(HttpMethod.HEAD, "/admin-web/**")))
                .authorizeHttpRequests(config -> config.anyRequest().permitAll())
                .build();
    }
}
