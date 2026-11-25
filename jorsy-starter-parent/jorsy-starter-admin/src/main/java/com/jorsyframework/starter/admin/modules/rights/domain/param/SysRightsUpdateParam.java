package com.jorsyframework.starter.admin.modules.rights.domain.param;

import com.jorsyframework.starter.admin.modules.rights.domain.enums.DataScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SysRightsUpdateParam(
        @Schema(description = "权限身份主键") @NotBlank @Size(min = 32, max = 32) String sysRightsId,
        @Schema(description = "版本号") @NotNull @PositiveOrZero Long version,
        @Schema(description = "用户主键") @NotBlank @Size(min = 32, max = 32) String sysUserId,
        @Schema(description = "组织主键") @NotBlank @Size(min = 32, max = 32) String sysOrgId,
        @Schema(description = "角色主键") @NotBlank @Size(min = 32, max = 32) String sysRoleId,
        @Schema(description = "基础数据权限：1仅本人 2本组织 3本组织及下级 9全部") @NotNull DataScope dataScope,
        @Schema(description = "是否启用") @NotNull Boolean enabled,
        @Schema(description = "是否默认") @NotNull Boolean defaulted,
        @Schema(description = "额外组织主键") List<@NotBlank @Size(min = 32, max = 32) String> extraOrgIds) {}
