package com.jopsonframework.starter.admin.modules.basedata.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SysConfigUpdateParam(
        @Schema(description = "配置主键") @NotBlank @Size(min = 32, max = 32) String sysConfigId,
        @Schema(description = "版本号") @NotNull @PositiveOrZero Long version,
        @Schema(description = "配置代码") @NotBlank @Size(max = 100) String sysConfigCode,
        @Schema(description = "配置名称") @NotBlank @Size(max = 100) String sysConfigName,
        @Schema(description = "配置值") @Size(max = 1000) String sysConfigValue,
        @Schema(description = "备注") @Size(max = 500) String remark) {}
