package com.jorsyframework.starter.admin.modules.basedata.facade;

import com.jorsyframework.starter.admin.modules.basedata.domain.dto.SysDictItemRow;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictItemCreateParam;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictItemQuery;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictItemUpdateParam;
import com.jorsyframework.starter.core.page.PageResult;

public interface ISysDictItemFacade {
    PageResult<SysDictItemRow> page(SysDictItemQuery param);

    SysDictItemRow detail(String id);

    String create(SysDictItemCreateParam param);

    String edit(SysDictItemUpdateParam param);

    void delete(String id, long version);
}
