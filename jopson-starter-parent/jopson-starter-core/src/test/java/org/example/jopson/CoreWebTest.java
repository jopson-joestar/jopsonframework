package org.example.jopson;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CoreTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CoreWebTest {
    private RestTestClient client;

    @BeforeEach
    void connect(@Value("${local.server.port}") int port) {
        client = RestTestClient.bindToServer().baseUrl("http://127.0.0.1:" + port).build();
    }

    @Test
    void serializesBusinessTypesThroughMvc() {
        String quote = """
                {"name":"样品","day":"2026-01-02","time":"2026-01-02 03:04:05",
                 "offset":"2026-01-02T03:04:05+08:00","amount":"12345678901234567890.1200"}
                """;
        String expected = """
                {"code":0,"message":"操作成功","data":{"name":"样品","day":"2026-01-02","time":"2026-01-02 03:04:05",
                 "offset":"2026-01-02T03:04:05+08:00","amount":"12345678901234567890.12"}}
                """;
        client.post().uri("/quote").contentType(MediaType.APPLICATION_JSON).body(quote).exchange()
                .expectStatus().isOk().expectBody().json(expected, JsonCompareMode.STRICT);
    }

    @Test
    void validatesPagingBoundsAndDefaults() {
        client.get().uri("/page").exchange().expectStatus().isOk().expectBody().json("""
                {"code":0,"message":"操作成功","data":{"items":[],"total":0,"currentPage":1,"pageSize":20}}
                """, JsonCompareMode.STRICT);
        client.get().uri("/page?currentPage=2&pageSize=501").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.data.pageSize").isEqualTo(501);
        for (String query : new String[]{"pageSize=0", "currentPage=0"}) {
            client.get().uri("/page?" + query).exchange().expectStatus().isBadRequest()
                    .expectBody().jsonPath("$.code").isEqualTo(2001);
        }
    }

    @Test
    void distinguishesValidationFromMalformedJson() {
        client.post().uri("/quote").contentType(MediaType.APPLICATION_JSON).body("{}").exchange()
                .expectStatus().isBadRequest().expectBody().jsonPath("$.code").isEqualTo(2001);
        client.get().uri("/count?count=0").exchange().expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo(2001);
        client.post().uri("/quote").contentType(MediaType.APPLICATION_JSON).body("{").exchange()
                .expectStatus().isBadRequest().expectBody().jsonPath("$.code").isEqualTo(2002);
    }

    @Test
    void preservesHttpSemanticsAndHidesFailureDetails() {
        client.get().uri("/quote").exchange().expectStatus().isEqualTo(405)
                .expectHeader().valueMatches("Allow", ".*POST.*");
        client.get().uri("/unmapped-path").exchange().expectStatus().isNotFound()
                .expectBody().jsonPath("$.code").isEqualTo(404);
        client.get().uri("/business").exchange().expectStatus().isBadRequest().expectBody().json("""
                {"code":3001,"message":"订单已关闭","data":null}
                """, JsonCompareMode.STRICT);
        client.get().uri("/failure").exchange().expectStatus().isEqualTo(500).expectBody().json("""
                {"code":5001,"message":"系统异常","data":null}
                """, JsonCompareMode.STRICT);
    }

    @Test
    void keepsDocumentationClosedByDefault() {
        for (String path : new String[]{"/v3/api-docs", "/v3/api-docs/swagger-config",
                "/swagger-ui.html", "/swagger-ui/index.html"}) {
            client.get().uri(path).exchange().expectStatus().isNotFound();
        }
    }

    @Test
    void exposesOnlyHealthByDefault(@Autowired ApplicationContext context) {
        client.get().uri("/actuator/health").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("UP");
        for (String path : new String[]{"/actuator/env", "/actuator/heapdump", "/actuator/metrics"}) {
            client.get().uri(path).exchange().expectStatus().isNotFound();
        }
        assertThat(context.getBeansOfType(DataSource.class)).isEmpty();
        assertThat(context.containsBean("redisConnectionFactory")).isFalse();
        assertThat(context.containsBean("redissonClient")).isFalse();
    }

    @Test
    void cachesAndEvictsUsingSpringAnnotations(@Autowired CoreTestApplication.CountingService service,
                                               @Autowired CacheManager manager) {
        assertThat(manager).isInstanceOf(CaffeineCacheManager.class);
        int initial = service.read("key");
        assertThat(service.read("key")).isEqualTo(initial);
        service.evict("key");
        assertThat(service.read("key")).isEqualTo(initial + 1);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void propagatesRequestIdIntoAsyncWorkAndLogsOnlyMetadata(CapturedOutput output) {
        String id = "sample-123";
        client.get().uri("/request-id?secret=query-secret-4711").header("X-Request-Id", id)
                .header("Authorization", "Bearer token-secret-4711").header("Cookie", "sid=cookie-secret-4711")
                .exchange().expectStatus().isOk().expectHeader().valueEquals("X-Request-Id", id)
                .expectBody(String.class).isEqualTo(id);

        client.post().uri("/quote").contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\":\"body-secret-4711\"}").exchange().expectStatus().isOk();

        String previous = id;
        for (String supplied : new String[]{"bad id", ""}) {
            var response = client.get().uri("/request-id").header("X-Request-Id", supplied)
                    .exchange().expectStatus().isOk().expectBody(String.class).returnResult();
            String generated = response.getResponseHeaders().getFirst("X-Request-Id");
            assertThat(generated).matches("[0-9a-f-]{36}").isNotEqualTo(previous);
            assertThat(response.getResponseBody()).isEqualTo(generated);
            previous = generated;
        }

        assertThat(output.getOut()).contains("requestId=sample-123 method=GET path=/request-id status=200")
                .doesNotContain("query-secret-4711", "token-secret-4711", "cookie-secret-4711", "body-secret-4711");
    }
}
