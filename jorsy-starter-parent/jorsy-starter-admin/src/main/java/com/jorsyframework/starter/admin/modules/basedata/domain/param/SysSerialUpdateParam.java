package com.jorsyframework.starter.admin.modules.basedata.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SysSerialUpdateParam(
        @Schema(description = "序列主键") @NotBlank @Size(min = 32, max = 32) String sysSerialId,
        @Schema(description = "版本号") @NotNull @PositiveOrZero Long version,
        @Schema(description = "序列名称") @NotBlank @Size(max = 100) String serialName,
        @Schema(description = "序列前缀") @Size(max = 50) String serialPrefix,
        @Schema(description = "序列后缀") @Size(max = 50) String serialSuffix,
        @Schema(description = "序列位数") @NotNull @Min(1) @Max(18) Integer serialDigit,
        @Schema(description = "日期格式化") @Size(max = 50) String dateFormat) {}
