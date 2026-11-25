package com.jorsyframework.starter.core.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.DefaultPropertiesPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

public class SpringdocDefaults implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> defaults = new HashMap<>();
        for (String property : List.of("springdoc.api-docs.enabled", "springdoc.swagger-ui.enabled")) {
            if (!environment.containsProperty(property)) {
                defaults.put(property, false);
            }
        }
        DefaultPropertiesPropertySource.addOrMerge(defaults, environment.getPropertySources());
    }
}
