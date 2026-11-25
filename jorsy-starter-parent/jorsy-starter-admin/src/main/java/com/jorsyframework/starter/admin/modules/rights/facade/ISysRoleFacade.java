package com.jorsyframework.starter.admin.modules.rights.facade;

import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysRoleRow;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysRoleCreateParam;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysRoleQuery;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysRoleUpdateParam;
import com.jorsyframework.starter.core.page.PageResult;
import java.util.List;

public interface ISysRoleFacade {
    PageResult<SysRoleRow> page(SysRoleQuery param);

    SysRoleRow detail(String id);

    String create(SysRoleCreateParam param);

    String edit(SysRoleUpdateParam param);

    void delete(String id, long version);

    List<SysRoleRow> tree();
}
