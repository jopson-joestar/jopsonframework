package org.example.jorsy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jorsyframework.starter.core.convert.ConvertProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CoreTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "springdoc.api-docs.enabled=true")
@Import({DocumentEndpoints.class, DocumentRowMapperImpl.class})
class DocumentContractTest {
    @MockitoBean
    ConvertProvider<String, String> reviewers;

    private RestTestClient client;

    @BeforeEach
    void connect(@Value("${local.server.port}") int port) {
        client = RestTestClient.bindToServer().baseUrl("http://127.0.0.1:" + port).build();
    }

    @Test
    void separatesCreateAndUpdateInputs() {
        client.post().uri("/documents").contentType(MediaType.APPLICATION_JSON).body("""
                {"title":"样品"}
                """).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.code").isEqualTo(2001);
        client.post().uri("/documents").contentType(MediaType.APPLICATION_JSON).body("""
                {"number":"D001","title":"样品"}
                """).exchange().expectStatus().isOk().expectBody().jsonPath("$.data.number").isEqualTo("D001");
        client.put().uri("/documents/1").contentType(MediaType.APPLICATION_JSON).body("""
                {"title":"修订","remark":"已核实"}
                """).exchange().expectStatus().isOk().expectBody().json("""
                {"code":0,"message":"操作成功","data":{"title":"修订","remark":"已核实"}}
                """, JsonCompareMode.STRICT);
    }

    @Test
    void bindsUrlAndJsonUsingNativeCodeTypes() {
        String expected = """
                {"status":"approved","level":20,"visibility":"PUBLIC"}
                """;
        client.get().uri("/documents/filters/20?status=approved&visibility=PUBLIC").exchange().expectStatus().isOk()
                .expectBody().json(expected, JsonCompareMode.STRICT);
        client.post().uri("/documents/filters").contentType(MediaType.APPLICATION_JSON).body(expected).exchange()
                .expectStatus().isOk().expectBody().json(expected, JsonCompareMode.STRICT);
        client.get().uri("/documents/filters/10").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.status").isEmpty();
    }

    @Test
    void rejectsUnknownCodesAndWrongJsonTypes() {
        for (String path : new String[]{"/documents?status=APPROVED", "/documents/filters/1",
                "/documents/filters/URGENT", "/documents/filters/20?status=other"}) {
            client.get().uri(path).exchange().expectStatus().isBadRequest().expectBody()
                    .jsonPath("$.code").isEqualTo(2001);
        }
        for (String body : new String[]{"{\"status\":\"APPROVED\"}", "{\"status\":2}", "{\"level\":1}",
                "{\"level\":\"20\"}", "{\"level\":20.0}", "{\"level\":{}}"}) {
            client.post().uri("/documents/filters").contentType(MediaType.APPLICATION_JSON).body(body).exchange()
                    .expectStatus().isBadRequest().expectBody().jsonPath("$.code").isEqualTo(2001);
        }
        for (String malformed : new String[]{"{\"level\":", "{\"level\":{"}) {
            client.post().uri("/documents/filters").contentType(MediaType.APPLICATION_JSON).body(malformed).exchange()
                    .expectStatus().isBadRequest().expectBody().jsonPath("$.code").isEqualTo(2002);
        }
    }

    @Test
    void documentsTheSameTypesAndAllowedValues() {
        var body = client.get().uri("/v3/api-docs").exchange().expectStatus().isOk().expectBody();
        String properties = "$.components.schemas.Filters.properties.";
        body.jsonPath(properties + "status.type").isEqualTo("string")
                .jsonPath(properties + "status.enum").isEqualTo(List.of("draft", "approved"))
                .jsonPath(properties + "level.type").isEqualTo("integer")
                .jsonPath(properties + "level.enum").isEqualTo(List.of(10, 20))
                .jsonPath(properties + "visibility.enum").isEqualTo(List.of("PUBLIC", "PRIVATE"))
                .jsonPath("$.paths['/documents/filters/{level}'].get.parameters[?(@.name == 'level')].schema.type")
                .isEqualTo(List.of("integer"))
                .jsonPath("$.paths['/documents/filters/{level}'].get.parameters[?(@.name == 'status')].schema.type")
                .isEqualTo(List.of("string"));
    }

    @Test
    void looksUpNamesOncePerPageAndKeepsMetadataAndOrder() {
        var keys = Set.of("user-1", "missing");
        when(reviewers.getAll(keys)).thenReturn(Map.of("user-1", "张三")).thenReturn(Map.of("user-1", "李四"));
        String path = "/documents?status=approved&currentPage=2&pageSize=20";
        client.get().uri(path).exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.data.total").isEqualTo(41).jsonPath("$.data.currentPage").isEqualTo(2)
                .jsonPath("$.data.pageSize").isEqualTo(20).jsonPath("$.data.items.length()").isEqualTo(20)
                .jsonPath("$.data.items[0].id").isEqualTo(21).jsonPath("$.data.items[0].reviewerName").isEmpty()
                .jsonPath("$.data.items[1].id").isEqualTo(22).jsonPath("$.data.items[1].reviewerName").isEqualTo("张三")
                .jsonPath("$.data.items[19].id").isEqualTo(40);
        verify(reviewers).getAll(keys);
        client.get().uri(path).exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.data.items[1].reviewerName").isEqualTo("李四");
        verify(reviewers, times(2)).getAll(keys);
        verifyNoMoreInteractions(reviewers);
    }

    @Test
    void skipsEmptyPagesAndPropagatesLookupFailures() {
        client.get().uri("/documents?status=draft").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.data.items").isEmpty().jsonPath("$.data.total").isEqualTo(0);
        verifyNoInteractions(reviewers);
        when(reviewers.getAll(anySet())).thenThrow(new IllegalStateException("lookup failed"));
        client.get().uri("/documents").exchange().expectStatus().isEqualTo(500).expectBody()
                .jsonPath("$.code").isEqualTo(5001);
    }
}
