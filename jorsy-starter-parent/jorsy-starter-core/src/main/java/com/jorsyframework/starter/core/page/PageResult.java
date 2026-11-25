package com.jorsyframework.starter.core.page;

import java.util.List;

public record PageResult<T>(List<T> items, long total, long currentPage, int pageSize) {

    public PageResult {
        items = List.copyOf(items);
    }
}
