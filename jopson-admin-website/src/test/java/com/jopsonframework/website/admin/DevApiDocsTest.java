package com.jopsonframework.website.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@SpringBootTest(classes = AdminWebsiteApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DevApiDocsTest {

    @Test
    void loadsSwaggerUiAndOpenApiDocument(@Value("${local.server.port}") int port) {
        var client = RestTestClient.bindToServer().baseUrl("http://127.0.0.1:" + port).build();
        client.get().uri("/swagger-ui/index.html").exchange().expectStatus().isOk()
                .expectBody(String.class).value(html -> assertThat(html).contains("swagger-ui-bundle.js"));
        client.get().uri("/v3/api-docs/swagger-config").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.url").isEqualTo("/v3/api-docs");
        client.get().uri("/v3/api-docs").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.openapi").value(String.class, version -> assertThat(version).startsWith("3."))
                .jsonPath("$.paths").isMap();
    }
}
