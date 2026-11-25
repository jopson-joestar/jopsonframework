package com.jopsonframework.starter.admin.common.autoconfigure;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class AdminAutoConfigurationFilter implements AutoConfigurationImportFilter, EnvironmentAware {
    private final Set<String> excluded = new HashSet<>();

    @Override
    public void setEnvironment(Environment environment) {
        var properties = Binder.get(environment);
        boolean admin = properties.bind("jopson.admin.enabled", Boolean.class).orElse(true);
        String mode = admin ? properties.bind("jopson.admin.mode", String.class).orElse("full") : "";
        boolean full = mode.equalsIgnoreCase("full");
        if (admin && !full && !mode.equalsIgnoreCase("simple")) {
            throw new IllegalStateException("jopson.admin.mode 必须为 full 或 simple");
        }

        boolean database = properties.bind("jopson.datasource.enabled", Boolean.class).orElse(full);
        if (full && !database) {
            throw new IllegalStateException("full 后台要求 jopson.datasource.enabled=true");
        }
        if (!database) {
            excluded.add("org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration");
        }
        if (!admin) {
            excluded.add("org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration");
            excluded.add("org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration");
            excluded.add("org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration");
        }
        if (!properties.bind("spring.cache.type", String.class).orElse("").equalsIgnoreCase("redis")) {
            excluded.add("org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration");
            excluded.add("org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration");
            excluded.add("org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration");
        }
    }

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata metadata) {
        boolean[] matches = new boolean[autoConfigurationClasses.length];
        for (int index = 0; index < matches.length; index++) {
            matches[index] = !excluded.contains(autoConfigurationClasses[index]);
        }
        return matches;
    }
}
