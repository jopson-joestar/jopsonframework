package com.jorsyframework.starter.admin.modules.basedata.facade;

import com.jorsyframework.starter.admin.modules.basedata.domain.dto.SysConfigRow;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysConfigCreateParam;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysConfigQuery;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysConfigUpdateParam;
import com.jorsyframework.starter.core.page.PageResult;

public interface ISysConfigFacade {
    PageResult<SysConfigRow> page(SysConfigQuery param);

    SysConfigRow detail(String id);

    String create(SysConfigCreateParam param);

    String edit(SysConfigUpdateParam param);

    void delete(String id, long version);

    String value(String sysConfigCode);
}
