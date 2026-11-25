package com.jorsyframework.starter.admin.modules.rights.domain.param;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jorsyframework.starter.admin.modules.rights.domain.enums.SysUserStatus;
import com.jorsyframework.starter.core.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;

public record SysUserPageQuery(
        @Schema(description = "当前页，默认 1") @Positive Long currentPage,
        @Schema(description = "每页条数，默认 20") @Positive Integer pageSize,
        @Schema(description = "登录账号，模糊匹配") String sysUserCode,
        @Schema(description = "用户名称，模糊匹配") String sysUserName,
        @Schema(description = "用户状态编码") SysUserStatus sysUserStatus,
        @Schema(description = "创建时间起点（含）") LocalDateTime createTimeBegin,
        @Schema(description = "创建时间终点（含该秒）") LocalDateTime createTimeEnd) {

    public PageQuery pagination() {
        return new PageQuery(currentPage, pageSize);
    }

    @JsonIgnore
    @AssertTrue(message = "创建时间起点必须在结束秒以内")
    public boolean isTimeRangeValid() {
        return createTimeBegin == null || createTimeEnd == null
                || !createTimeBegin.isAfter(createTimeEnd.withNano(999_999_999));
    }
}
