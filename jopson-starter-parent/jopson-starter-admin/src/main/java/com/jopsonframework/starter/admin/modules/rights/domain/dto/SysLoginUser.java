package com.jopsonframework.starter.admin.modules.rights.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jopsonframework.starter.admin.modules.rights.domain.enums.DataScope;
import java.util.List;
import java.util.Set;

public record SysLoginUser(String userId, String username, String realName, Identity identity,
                           List<Identity> identities, Set<String> permissions, boolean administrator,
                           List<String> extraOrgIds) {
    @JsonProperty("mode")
    public String mode() {
        return identity == null ? "simple" : "full";
    }

    public record Identity(String sysRightsId, String sysOrgId, String sysOrgName, String sysRoleId,
                           String sysRoleCode, String sysRoleName, DataScope dataScope) {}
}
