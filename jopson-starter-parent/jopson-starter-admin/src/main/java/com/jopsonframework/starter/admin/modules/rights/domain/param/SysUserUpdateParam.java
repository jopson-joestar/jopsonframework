package com.jopsonframework.starter.admin.modules.rights.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SysUserUpdateParam(
        @Schema(description = "用户主键") @NotBlank @Size(min = 32, max = 32) String sysUserId,
        @Schema(description = "最后读取的版本号") @NotNull @PositiveOrZero Long version,
        @Schema(description = "用户名称") @NotBlank @Size(max = 100) String sysUserName,
        @Schema(description = "手机号") @Size(max = 30) String sysUserMobile,
        @Schema(description = "邮箱") @Email @Size(max = 100) String sysUserEmail,
        @Schema(description = "QQ 号") @Size(max = 30) String sysUserQq) {}
