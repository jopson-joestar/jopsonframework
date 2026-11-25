package com.jorsyframework.starter.admin.modules.rights.domain.dto;

import com.jorsyframework.starter.admin.modules.rights.domain.enums.SysOrgType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysOrgRow {
    @Schema(description = "组织主键")
    private String sysOrgId;

    @Schema(description = "父级主键")
    private String parentId;

    @Schema(description = "组织代码")
    private String sysOrgCode;

    @Schema(description = "组织名称")
    private String sysOrgName;

    @Schema(description = "类型代码")
    private SysOrgType sysOrgType;

    @Schema(description = "排序序号")
    private Integer sortNo;

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

    @Schema(description = "下级节点")
    private List<SysOrgRow> children = new ArrayList<>();
}
