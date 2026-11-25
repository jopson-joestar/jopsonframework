package com.jorsyframework.starter.admin.modules.rights.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SysRoleUpdateParam(
        @Schema(description = "角色主键") @NotBlank @Size(min = 32, max = 32) String sysRoleId,
        @Schema(description = "版本号") @NotNull @PositiveOrZero Long version,
        @Schema(description = "父级角色主键") @NotBlank @Size(min = 32, max = 32) String parentId,
        @Schema(description = "角色代码") @NotBlank @Size(max = 50) String sysRoleCode,
        @Schema(description = "角色名称") @NotBlank @Size(max = 100) String sysRoleName,
        @Schema(description = "备注") @Size(max = 500) String remark) {}
