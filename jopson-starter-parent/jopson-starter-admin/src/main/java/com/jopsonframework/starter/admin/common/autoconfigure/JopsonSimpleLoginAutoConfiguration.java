package com.jopsonframework.starter.admin.common.autoconfigure;

import com.jopsonframework.starter.admin.common.security.LoginCsrfController;
import com.jopsonframework.starter.admin.simple.SimpleLoginConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

@AutoConfiguration(beforeName = {
        "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration"})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "jopson.admin", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "jopson.admin", name = "mode", havingValue = "simple")
@Import({SimpleLoginConfiguration.class, LoginCsrfController.class})
public class JopsonSimpleLoginAutoConfiguration {
}
