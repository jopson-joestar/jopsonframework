package com.jorsyframework.starter.core.mybatis;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jorsyframework.starter.core.page.PageQuery;
import com.jorsyframework.starter.core.page.PageResult;

public final class MybatisPages {
    private MybatisPages() {}

    public static <T> Page<T> toPage(PageQuery query) {
        if (query.currentPage() < 1 || query.pageSize() < 1) {
            throw new IllegalArgumentException("页码和每页条数必须为正数");
        }
        return new Page<>(query.currentPage(), query.pageSize());
    }

    public static <T> PageResult<T> toResult(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), Math.toIntExact(page.getSize()));
    }
}
