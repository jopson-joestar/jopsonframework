package com.jopsonframework.website.admin;

import java.util.List;

import javax.sql.DataSource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jopsonframework.starter.admin.modules.rights.mapper.SysUserMapper;
import com.jopsonframework.website.admin.mapping.ItemTestMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@SpringBootTest(classes = AdminWebsiteApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "jopson.admin.mode=ignored-while-disabled")
class AdminReuseTest {

    @Test
    void startsDevWithoutAdminInfrastructure(@Autowired ApplicationContext context) {
        assertThat(context.getBeansOfType(DataSource.class)).isEmpty();
        assertThat(context.getBeansOfType(SecurityFilterChain.class)).isEmpty();
        assertThat(context.getBeansOfType(SysUserMapper.class)).isEmpty();
        assertThat(context.getBeansOfType(RedisConnectionFactory.class)).isEmpty();
        for (String type : List.of("com.alibaba.druid.pool.DruidDataSource", "com.p6spy.engine.spy.P6SpyDriver")) {
            assertThat(ClassUtils.isPresent(type, context.getClassLoader())).as(type).isFalse();
        }
    }

    @Test
    void injectsGeneratedMapperAndUsesCoreUtilities(@Autowired ItemTestMapper mapper) {
        var item = new ItemTestMapper.Item(StrUtil.trim("  样品  "), CollUtil.size(List.of("A", "B")));
        assertThat(mapper.toDto(item)).isEqualTo(new ItemTestMapper.ItemDto("样品", 2));
    }

    @Test
    void userManagementIsAbsentInDev(@Value("${local.server.port}") int port) {
        var client = RestTestClient.bindToServer().build();
        for (String path : List.of("/auth/csrf", "/admin/sys-login/current", "/admin-web/", "/admin-web/index.html")) {
            client.get().uri("http://127.0.0.1:" + port + path).exchange().expectStatus().isNotFound();
        }
        RestTestClient.bindToServer().build()
                .post().uri("http://127.0.0.1:" + port + "/admin/sys-user/page")
                .contentType(MediaType.APPLICATION_JSON).body("{}")
                .exchange().expectStatus().isNotFound();
    }

    @Test
    void reportsHealthyInDev(@Value("${local.server.port}") int port) {
        RestTestClient.bindToServer().build()
                .get().uri("http://127.0.0.1:" + port + "/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("UP");
    }
}
