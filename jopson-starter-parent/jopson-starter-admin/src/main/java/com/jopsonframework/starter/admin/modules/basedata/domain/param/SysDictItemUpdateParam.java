package com.jopsonframework.starter.admin.modules.basedata.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

public record SysDictItemUpdateParam(
        @Schema(description = "字典明细主键") @NotBlank @Size(min = 32, max = 32) String sysDictItemId,
        @Schema(description = "版本号") @NotNull @PositiveOrZero Long version,
        @Schema(description = "字典主键") @NotBlank @Size(min = 32, max = 32) String sysDictId,
        @Schema(description = "明细值") @NotBlank @Size(max = 50) String dictItemValue,
        @Schema(description = "明细标签") @NotBlank @Size(max = 100) String dictItemLabel,
        @Schema(description = "排序序号") Integer sortNo,
        @Schema(description = "明细扩展数据") JsonNode dictItemExtra,
        @Schema(description = "标签颜色") @Size(max = 50) String labelColor,
        @Schema(description = "标签样式") @Size(max = 100) String labelClass,
        @Schema(description = "是否禁用") Boolean disabled,
        @Schema(description = "备注") @Size(max = 500) String remark) {}
