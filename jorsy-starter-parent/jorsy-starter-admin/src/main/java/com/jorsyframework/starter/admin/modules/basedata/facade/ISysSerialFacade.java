package com.jorsyframework.starter.admin.modules.basedata.facade;

import com.jorsyframework.starter.admin.modules.basedata.domain.dto.SysSerialRow;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysSerialCreateParam;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysSerialQuery;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysSerialUpdateParam;
import com.jorsyframework.starter.core.page.PageResult;

public interface ISysSerialFacade {
    PageResult<SysSerialRow> page(SysSerialQuery param);

    SysSerialRow detail(String id);

    String create(SysSerialCreateParam param);

    String edit(SysSerialUpdateParam param);

    void delete(String id, long version);

    String nextValue(String serialCode);
}
