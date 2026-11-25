package com.jopsonframework.starter.admin.modules.rights.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

public record SysRoleQuery(
        @Schema(description = "当前页") @Min(1) Long currentPage,
        @Schema(description = "每页条数") @Min(1) Integer pageSize,
        @Schema(description = "角色代码") String sysRoleCode,
        @Schema(description = "角色名称") String sysRoleName,
        @Schema(description = "创建时间起") LocalDateTime createTimeBegin,
        @Schema(description = "创建时间止") LocalDateTime createTimeEnd) {}
