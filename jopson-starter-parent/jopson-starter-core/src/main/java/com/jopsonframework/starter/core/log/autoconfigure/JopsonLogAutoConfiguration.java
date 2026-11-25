package com.jopsonframework.starter.core.log.autoconfigure;

import com.jopsonframework.starter.core.log.RequestIdFilter;
import com.jopsonframework.starter.core.log.RequestIdTaskDecorator;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.task.TaskDecorator;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JopsonLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "jopsonRequestIdFilter")
    FilterRegistrationBean<RequestIdFilter> jopsonRequestIdFilter() {
        var registration = new FilterRegistrationBean<>(new RequestIdFilter());
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(TaskDecorator.class)
    RequestIdTaskDecorator jopsonRequestIdTaskDecorator() {
        return new RequestIdTaskDecorator();
    }
}
