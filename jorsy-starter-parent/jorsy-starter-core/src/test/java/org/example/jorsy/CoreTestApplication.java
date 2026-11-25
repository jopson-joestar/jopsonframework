package org.example.jorsy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import com.jorsyframework.starter.core.exception.BusinessException;
import com.jorsyframework.starter.core.log.RequestIdFilter;
import com.jorsyframework.starter.core.page.PageQuery;
import com.jorsyframework.starter.core.page.PageResult;
import com.jorsyframework.starter.core.result.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.MDC;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootConfiguration
// core 自身的测试类路径包含可选依赖，此站点专门验证无数据源的 MVC 用法。
@EnableAutoConfiguration(excludeName = {
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration",
        "com.alibaba.druid.spring.boot4.autoconfigure.DruidDataSourceAutoConfigure"
})
@Import({CoreTestApplication.Endpoints.class, CoreTestApplication.CountingService.class})
class CoreTestApplication {

    record Quote(@NotBlank String name, LocalDate day, LocalDateTime time, OffsetDateTime offset, BigDecimal amount) {}

    @RestController
    static class Endpoints {
        @PostMapping("/quote")
        R<Quote> quote(@Valid @RequestBody Quote quote) {
            return R.success(quote);
        }

        @GetMapping("/page")
        R<PageResult<String>> page(@Valid @ModelAttribute PageQuery query) {
            return R.success(new PageResult<>(List.of(), 0, query.currentPage(), query.pageSize()));
        }

        @GetMapping("/count")
        R<Integer> count(@RequestParam @Min(1) int count) {
            return R.success(count);
        }

        @GetMapping("/business")
        void business() {
            throw new BusinessException("订单已关闭");
        }

        @GetMapping("/failure")
        void failure() {
            throw new IllegalStateException("internal detail");
        }

        @GetMapping("/request-id")
        Callable<String> asyncRequestId() {
            return () -> MDC.get(RequestIdFilter.MDC_KEY);
        }
    }

    static class CountingService {
        private final AtomicInteger reads = new AtomicInteger();

        @Cacheable("counter")
        public int read(String key) {
            return reads.incrementAndGet();
        }

        @CacheEvict("counter")
        public void evict(String key) {
        }
    }
}
