package com.jopsonframework.starter.admin.modules.basedata.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Getter
@Setter
public class SysDictItemRow {
    @Schema(description = "字典明细主键")
    private String sysDictItemId;

    @Schema(description = "字典主键")
    private String sysDictId;

    @Schema(description = "明细值")
    private String dictItemValue;

    @Schema(description = "明细标签")
    private String dictItemLabel;

    @Schema(description = "排序序号")
    private Integer sortNo;

    @Schema(description = "明细扩展数据")
    private JsonNode dictItemExtra;

    @Schema(description = "标签颜色")
    private String labelColor;

    @Schema(description = "标签样式")
    private String labelClass;

    @Schema(description = "是否禁用")
    private Boolean disabled;

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
