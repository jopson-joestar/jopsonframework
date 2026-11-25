package com.jorsyframework.starter.admin.modules.basedata.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.jorsyframework.starter.admin.modules.basedata.domain.entity.SysDictItem;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictItemQuery;
import com.jorsyframework.starter.core.page.PageResult;
import java.util.List;

public interface ISysDictItemService extends IService<SysDictItem> {
    List<SysDictItem> forDict(String id);

    PageResult<SysDictItem> page(SysDictItemQuery param);

    SysDictItem lockById(String id);

    void delete(SysDictItem row, String actor);
}
