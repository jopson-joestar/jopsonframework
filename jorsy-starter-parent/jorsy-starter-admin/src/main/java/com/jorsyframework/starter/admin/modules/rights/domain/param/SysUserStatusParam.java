package com.jorsyframework.starter.admin.modules.rights.domain.param;

import com.jorsyframework.starter.admin.modules.rights.domain.enums.SysUserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SysUserStatusParam(
        @Schema(description = "用户主键") @NotBlank @Size(min = 32, max = 32) String sysUserId,
        @Schema(description = "目标状态编码") @NotNull SysUserStatus sysStatus,
        @Schema(description = "最后读取的版本号") @NotNull @PositiveOrZero Long version) {}
