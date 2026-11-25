package com.jopsonframework.starter.core.page;

import jakarta.validation.constraints.Min;

public record PageQuery(@Min(1) Long currentPage, @Min(1) Integer pageSize) {

    public PageQuery {
        currentPage = currentPage == null ? 1L : currentPage;
        pageSize = pageSize == null ? 20 : pageSize;
    }
}
