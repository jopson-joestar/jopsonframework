package com.jorsyframework.starter.admin.modules.basedata.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysConfigRow {
    @Schema(description = "配置主键")
    private String sysConfigId;

    @Schema(description = "配置代码")
    private String sysConfigCode;

    @Schema(description = "配置名称")
    private String sysConfigName;

    @Schema(description = "配置值")
    private String sysConfigValue;

    @Schema(description = "备注")
    private String remark;

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
