package com.jopsonframework.starter.admin.modules.basedata.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysDict;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysDictQuery;
import com.jopsonframework.starter.core.page.PageResult;

public interface ISysDictService extends IService<SysDict> {
    SysDict byCode(String code);

    SysDict lockCode(String code);

    PageResult<SysDict> page(SysDictQuery param);

    SysDict lockById(String id);

    void delete(SysDict row, String actor);
}
