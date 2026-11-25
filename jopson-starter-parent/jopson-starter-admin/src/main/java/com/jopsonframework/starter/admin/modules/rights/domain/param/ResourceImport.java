package com.jopsonframework.starter.admin.modules.rights.domain.param;

import com.jopsonframework.starter.admin.common.enums.SyncOperation;
import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysResType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import tools.jackson.databind.JsonNode;

public record ResourceImport(@NotNull Mode mode, Set<@NotNull SyncOperation> operations,
                             @NotEmpty List<@NotNull @Valid Node> resources) {
    public enum Mode { FULL, INCREMENTAL }

    public record Node(Integer sortNo, @NotNull SysResType sysResType, @Size(max = 100) String permission,
                       @NotBlank @Size(max = 100) String routeName, @Size(max = 500) String routePath,
                       @Size(max = 500) String component, @Size(max = 500) String redirect,
                       JsonNode routeMeta, @Size(max = 500) String remark, List<@NotNull @Valid Node> children) {
        public SysResCreateParam form(String parentId) {
            return new SysResCreateParam(parentId, sortNo, sysResType, permission, routeName, routePath,
                    component, redirect, routeMeta, remark);
        }
    }
}
