package com.jorsyframework.starter.admin.modules.basedata.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.jorsyframework.starter.admin.modules.basedata.domain.entity.SysConfig;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysConfigQuery;
import com.jorsyframework.starter.core.page.PageResult;

public interface ISysConfigService extends IService<SysConfig> {
    SysConfig byCode(String code);

    PageResult<SysConfig> page(SysConfigQuery param);

    SysConfig lockById(String id);

    void delete(SysConfig row, String actor);
}
