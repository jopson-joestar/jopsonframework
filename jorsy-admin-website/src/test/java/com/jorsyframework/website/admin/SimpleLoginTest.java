package com.jorsyframework.website.admin;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.sql.DataSource;

import com.jorsyframework.starter.admin.modules.rights.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AdminWebsiteApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"jorsy.admin.mode=simple", "jorsy.admin.simple.username=operator", "server.servlet.session.cookie.secure=false"})
@Import(SimpleLoginTest.TestEndpoints.class)
class SimpleLoginTest {
    private static final String PASSWORD = "test-password";

    @Value("${local.server.port}")
    private int port;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private ApplicationContext context;

    @DynamicPropertySource
    static void credentials(DynamicPropertyRegistry properties) {
        var hash = "{bcrypt}" + new BCryptPasswordEncoder(4).encode(PASSWORD);
        properties.add("jorsy.admin.simple.password-hash", () -> hash);
    }

    @Test
    void protectsBusinessEndpointsWithAFixedAccountAndNoAdminDatabase() throws Exception {
        assertThat(context.getBeansOfType(DataSource.class)).isEmpty();
        assertThat(context.getBeansOfType(SysUserMapper.class)).isEmpty();
        try (var client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build()) {
            assertThat(get(client, "/business-test").statusCode()).isEqualTo(401);
            assertThat(post(client, "/auth/login", "username=operator&password=" + PASSWORD, false).statusCode())
                    .isEqualTo(403);
            assertThat(post(client, "/auth/login", "username=operator&password=incorrect", true).statusCode())
                    .isEqualTo(401);
            assertThat(post(client, "/auth/login", "username=operator&password=" + PASSWORD, true).statusCode())
                    .isEqualTo(200);

            var business = get(client, "/business-test");
            assertThat(business.statusCode()).isEqualTo(200);
            assertThat(business.body()).isEqualTo("available");
            var user = json.readTree(get(client, "/admin/sys-login/current").body()).path("data");
            assertThat(user.path("mode").asText()).isEqualTo("simple");
            assertThat(user.path("username").asText()).isEqualTo("operator");
            assertThat(user.path("administrator").asBoolean()).isFalse();
            assertThat(user.path("identities").isEmpty()).isTrue();
            assertThat(json.readTree(get(client, "/admin/sys-login/menu").body()).path("data").isEmpty()).isTrue();
            assertThat(post(client, "/admin/sys-user/page", "", true).statusCode()).isEqualTo(404);
            assertThat(post(client, "/admin/sys-login/switch-rights", "", true).statusCode()).isEqualTo(404);
            assertThat(post(client, "/auth/logout", "", true).statusCode()).isEqualTo(200);
            assertThat(get(client, "/business-test").statusCode()).isEqualTo(401);

            for (int failure = 1; failure < 20; failure++) {
                assertThat(post(client, "/auth/login", "username=operator&password=incorrect", true).statusCode())
                        .isEqualTo(401);
            }
            assertThat(post(client, "/auth/login", "username=operator&password=" + PASSWORD, true).statusCode())
                    .isEqualTo(429);
        }
    }

    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        return client.send(request(path).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(HttpClient client, String path, String form, boolean withCsrf) throws Exception {
        var request = request(path).header("Content-Type", "application/x-www-form-urlencoded");
        if (withCsrf) {
            var csrf = get(client, "/auth/csrf");
            assertThat(csrf.statusCode()).isEqualTo(200);
            var data = json.readTree(csrf.body()).path("data");
            request.header(data.path("headerName").asText(), data.path("token").asText());
        }
        return client.send(request.POST(HttpRequest.BodyPublishers.ofString(form)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestEndpoints {
        @Bean
        RouterFunction<ServerResponse> businessEndpoint() {
            return RouterFunctions.route().GET("/business-test", request -> ServerResponse.ok().body("available")).build();
        }
    }
}
