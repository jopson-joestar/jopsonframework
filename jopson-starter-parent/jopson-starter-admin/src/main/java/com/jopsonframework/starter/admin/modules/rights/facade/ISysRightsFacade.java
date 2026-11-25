package com.jopsonframework.starter.admin.modules.rights.facade;

import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysRightsRow;
import com.jopsonframework.starter.admin.modules.rights.domain.param.RightsEnabled;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRightsCreateParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRightsQuery;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRightsUpdateParam;
import com.jopsonframework.starter.core.page.PageResult;

public interface ISysRightsFacade {
    PageResult<SysRightsRow> page(SysRightsQuery param);

    SysRightsRow detail(String id);

    String create(SysRightsCreateParam param);

    String edit(SysRightsUpdateParam param);

    void delete(String id, long version);

    void enabled(RightsEnabled param);
}
