package com.jopsonframework.starter.admin.modules.basedata.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SysDictCreateParam(
        @Schema(description = "字典代码") @NotBlank @Size(max = 50) String sysDictCode,
        @Schema(description = "字典名称") @NotBlank @Size(max = 100) String sysDictName,
        @Schema(description = "备注") @Size(max = 500) String remark) {}
