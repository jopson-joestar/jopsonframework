package com.jorsyframework.starter.core.mybatis.autoconfigure;

import javax.sql.DataSource;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;

class JorsyMybatisAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JorsyMybatisAutoConfiguration.class));

    @Test
    void requiresBothDataSourceAndPaginationSupport() {
        runner.run(context -> assertThat(context).doesNotHaveBean(MybatisPlusInterceptor.class));
        runner.withBean(DataSource.class, DriverManagerDataSource::new)
                .withClassLoader(new FilteredClassLoader(PaginationInnerInterceptor.class))
                .run(context -> assertThat(context).doesNotHaveBean(MetaObjectHandler.class));
    }

    @Test
    void respectsSiteBeansAndConfiguredStrategies() {
        var custom = new MybatisPlusInterceptor();
        var audit = new AuditTimeHandler();
        runner.withBean(DataSource.class, DriverManagerDataSource::new)
                .withBean(MybatisPlusInterceptor.class, () -> custom)
                .withBean(MetaObjectHandler.class, () -> audit)
                .withPropertyValues("mybatis-plus.global-config.db-config.id-type=AUTO",
                        "mybatis-plus.global-config.db-config.update-strategy=NOT_NULL")
                .run(context -> {
                    assertThat(context.getBean(MybatisPlusInterceptor.class)).isSameAs(custom);
                    assertThat(context.getBean(MetaObjectHandler.class)).isSameAs(audit);
                    var properties = new MybatisPlusProperties();
                    context.getBean(MybatisPlusPropertiesCustomizer.class).customize(properties);
                    var db = properties.getGlobalConfig().getDbConfig();
                    assertThat(db.getIdType()).isEqualTo(IdType.AUTO);
                    assertThat(db.getUpdateStrategy()).isEqualTo(FieldStrategy.NOT_NULL);
                });
    }
}
