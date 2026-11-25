package com.jorsyframework.starter.admin.modules.basedata.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysSerialRow {
    @Schema(description = "序列主键")
    private String sysSerialId;

    @Schema(description = "序列代码")
    private String serialCode;

    @Schema(description = "序列名称")
    private String serialName;

    @Schema(description = "序列前缀")
    private String serialPrefix;

    @Schema(description = "序列后缀")
    private String serialSuffix;

    @Schema(description = "序列位数")
    private Integer serialDigit;

    @Schema(description = "日期格式化")
    private String dateFormat;

    @Schema(description = "系统数据")
    private Boolean sysData;

    @Schema(description = "创建人主键")
    private String createId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改人主键")
    private String updateId;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;

    @Schema(description = "版本号")
    private Long version;
}
