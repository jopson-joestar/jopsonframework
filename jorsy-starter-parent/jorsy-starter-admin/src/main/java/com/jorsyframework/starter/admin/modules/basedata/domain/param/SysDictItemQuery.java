package com.jorsyframework.starter.admin.modules.basedata.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

public record SysDictItemQuery(
        @Schema(description = "当前页") @Min(1) Long currentPage,
        @Schema(description = "每页条数") @Min(1) Integer pageSize,
        @Schema(description = "字典主键") String sysDictId,
        @Schema(description = "明细值") String dictItemValue,
        @Schema(description = "明细标签") String dictItemLabel,
        @Schema(description = "创建时间起") LocalDateTime createTimeBegin,
        @Schema(description = "创建时间止") LocalDateTime createTimeEnd) {}
