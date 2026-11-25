package com.jorsyframework.starter.admin.modules.rights.domain.dto;

import com.jorsyframework.starter.admin.modules.rights.domain.enums.DataScope;
import java.util.List;
import java.util.Set;

public record SysLoginUser(String userId, String username, String realName, Identity identity,
                           List<Identity> identities, Set<String> permissions, boolean administrator,
                           List<String> extraOrgIds) {
    public record Identity(String sysRightsId, String sysOrgId, String sysOrgName, String sysRoleId,
                           String sysRoleCode, String sysRoleName, DataScope dataScope) {}
}
