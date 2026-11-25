package com.jopsonframework.starter.admin.modules.rights.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

public record SysRightsQuery(
        @Schema(description = "当前页") @Min(1) Long currentPage,
        @Schema(description = "每页条数") @Min(1) Integer pageSize,
        @Schema(description = "用户主键") String sysUserId,
        @Schema(description = "组织主键") String sysOrgId,
        @Schema(description = "角色主键") String sysRoleId,
        @Schema(description = "是否启用") Boolean enabled,
        @Schema(description = "是否默认") Boolean defaulted,
        @Schema(description = "创建时间起") LocalDateTime createTimeBegin,
        @Schema(description = "创建时间止") LocalDateTime createTimeEnd) {}
