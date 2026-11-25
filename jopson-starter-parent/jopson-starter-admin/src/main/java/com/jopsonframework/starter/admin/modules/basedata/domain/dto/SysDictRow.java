package com.jopsonframework.starter.admin.modules.basedata.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysDictRow {
    @Schema(description = "字典主键")
    private String sysDictId;

    @Schema(description = "字典代码")
    private String sysDictCode;

    @Schema(description = "字典名称")
    private String sysDictName;

    @Schema(description = "是否锁定")
    private Boolean locked;

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
