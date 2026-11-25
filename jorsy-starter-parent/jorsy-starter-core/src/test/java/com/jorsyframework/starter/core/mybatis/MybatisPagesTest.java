package com.jorsyframework.starter.core.mybatis;

import com.jorsyframework.starter.core.page.PageQuery;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MybatisPagesTest {
    @Test
    void rejectsNonPositivePageAndSize() {
        assertThatThrownBy(() -> MybatisPages.toPage(new PageQuery(0L, 20))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MybatisPages.toPage(new PageQuery(1L, -1))).isInstanceOf(IllegalArgumentException.class);
    }
}
