package com.jorsyframework.starter.admin.common.autoconfigure;

import com.jorsyframework.starter.admin.modules.rights.access.SysUserOperatorProvider;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@AutoConfiguration(afterName = "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration")
public class JorsyAdminAutoConfiguration {
    private static final String MODULES = "com.jorsyframework.starter.admin.modules";

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = {"org.mybatis.spring.SqlSessionTemplate",
            "com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor"})
    @ConditionalOnSingleCandidate(SqlSessionTemplate.class)
    @MapperScan(basePackages = MODULES, annotationClass = Mapper.class)
    static class Persistence {
        @Bean
        static BeanDefinitionRegistryPostProcessor adminDataComponents() {
            return registry -> new ClassPathBeanDefinitionScanner(registry)
                    .scan(MODULES + ".*.service.impl", MODULES + ".*.converter");
        }

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnBean(SysUserOperatorProvider.class)
        static class Management {
            @Bean
            static BeanDefinitionRegistryPostProcessor adminFacades(Environment environment) {
                return registry -> new ClassPathBeanDefinitionScanner(registry, true, environment).scan(MODULES + ".*.facade.impl");
            }

            @Bean
            @ConditionalOnMissingBean
            PasswordEncoder sysUserPasswordEncoder() {
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
            }

            @Bean
            @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
            static BeanDefinitionRegistryPostProcessor adminControllers(Environment environment) {
                return registry -> new ClassPathBeanDefinitionScanner(registry, true, environment)
                        .scan(MODULES + ".*.controller", "com.jorsyframework.starter.admin.common.web");
            }
        }
    }
}
