package com.jorsyframework.starter.core.mybatis.autoconfigure;

import javax.sql.DataSource;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

@AutoConfiguration(afterName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        beforeName = "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration")
@ConditionalOnClass({MybatisPlusPropertiesCustomizer.class, PaginationInnerInterceptor.class, CCJSqlParserUtil.class})
@ConditionalOnSingleCandidate(DataSource.class)
public class JorsyMybatisAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    MybatisPlusInterceptor jorsyMybatisInterceptor() {
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    MetaObjectHandler jorsyAuditTimeHandler() {
        return new AuditTimeHandler();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    MybatisPlusPropertiesCustomizer jorsyMybatisDefaults(Environment environment) {
        var binder = Binder.get(environment);
        return properties -> {
            var db = properties.getGlobalConfig().getDbConfig();
            String prefix = "mybatis-plus.global-config.db-config.";
            db.setIdType(binder.bind(prefix + "id-type", IdType.class).orElse(IdType.ASSIGN_UUID));
            db.setUpdateStrategy(binder.bind(prefix + "update-strategy", FieldStrategy.class).orElse(FieldStrategy.ALWAYS));
        };
    }
}
