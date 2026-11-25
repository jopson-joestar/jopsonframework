package com.jopsonframework.starter.admin.modules.basedata.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SysDictUpdateParam(
        @Schema(description = "字典主键") @NotBlank @Size(min = 32, max = 32) String sysDictId,
        @Schema(description = "版本号") @NotNull @PositiveOrZero Long version,
        @Schema(description = "字典代码") @NotBlank @Size(max = 50) String sysDictCode,
        @Schema(description = "字典名称") @NotBlank @Size(max = 100) String sysDictName,
        @Schema(description = "备注") @Size(max = 500) String remark) {}
