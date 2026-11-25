package com.jorsyframework.starter.admin.common.autoconfigure;

import com.jorsyframework.starter.admin.common.security.LoginCsrfController;
import com.jorsyframework.starter.admin.common.web.AdminExceptionHandler;
import com.jorsyframework.starter.admin.modules.rights.access.SysLoginSecurityConfiguration;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@AutoConfiguration(
        afterName = "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
        beforeName = {
                "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration",
                "org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration"})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "jorsy.admin", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "jorsy.admin", name = "mode", havingValue = "full", matchIfMissing = true)
@ComponentScan(
        basePackages = JorsyAdminAutoConfiguration.MODULES,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SysLoginSecurityConfiguration.class))
@MapperScan(basePackages = JorsyAdminAutoConfiguration.MODULES, annotationClass = Mapper.class)
@Import({SysLoginSecurityConfiguration.class, LoginCsrfController.class, AdminExceptionHandler.class})
public class JorsyAdminAutoConfiguration {
    static final String MODULES = "com.jorsyframework.starter.admin.modules";

    @Bean
    @ConditionalOnMissingBean
    PasswordEncoder sysUserPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
