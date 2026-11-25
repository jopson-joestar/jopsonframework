package com.jopsonframework.starter.admin.modules.basedata.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

public record SysDictQuery(
        @Schema(description = "当前页") @Min(1) Long currentPage,
        @Schema(description = "每页条数") @Min(1) Integer pageSize,
        @Schema(description = "字典代码") String sysDictCode,
        @Schema(description = "字典名称") String sysDictName,
        @Schema(description = "是否锁定") Boolean locked,
        @Schema(description = "创建时间起") LocalDateTime createTimeBegin,
        @Schema(description = "创建时间止") LocalDateTime createTimeEnd) {}
