package com.jorsyframework.starter.admin.modules.basedata.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SysConfigCreateParam(
        @Schema(description = "配置代码") @NotBlank @Size(max = 100) String sysConfigCode,
        @Schema(description = "配置名称") @NotBlank @Size(max = 100) String sysConfigName,
        @Schema(description = "配置值") @Size(max = 1000) String sysConfigValue,
        @Schema(description = "备注") @Size(max = 500) String remark) {}
