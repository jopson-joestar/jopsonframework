package com.jorsyframework.starter.admin.modules.rights.facade;

import com.jorsyframework.starter.admin.modules.rights.domain.dto.RoleResourceTree;
import com.jorsyframework.starter.admin.modules.rights.domain.param.RoleResources;

public interface ISysRoleAuthFacade {
    RoleResourceTree list(String sysRoleId);
    void authorize(RoleResources param);
}
