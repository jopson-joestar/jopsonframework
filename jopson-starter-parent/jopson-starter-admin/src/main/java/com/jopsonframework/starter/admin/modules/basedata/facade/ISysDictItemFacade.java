package com.jopsonframework.starter.admin.modules.basedata.facade;

import com.jopsonframework.starter.admin.modules.basedata.domain.dto.SysDictItemRow;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysDictItemCreateParam;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysDictItemQuery;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysDictItemUpdateParam;
import com.jopsonframework.starter.core.page.PageResult;

public interface ISysDictItemFacade {
    PageResult<SysDictItemRow> page(SysDictItemQuery param);

    SysDictItemRow detail(String id);

    String create(SysDictItemCreateParam param);

    String edit(SysDictItemUpdateParam param);

    void delete(String id, long version);
}
