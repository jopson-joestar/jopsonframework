package com.jopsonframework.starter.admin.modules.rights.domain.param;

import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysOrgType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SysOrgUpdateParam(
        @Schema(description = "组织主键") @NotBlank @Size(min = 32, max = 32) String sysOrgId,
        @Schema(description = "版本号") @NotNull @PositiveOrZero Long version,
        @Schema(description = "组织名称") @NotBlank @Size(max = 100) String sysOrgName,
        @Schema(description = "类型代码") @NotNull SysOrgType sysOrgType,
        @Schema(description = "排序序号") Integer sortNo) {}
