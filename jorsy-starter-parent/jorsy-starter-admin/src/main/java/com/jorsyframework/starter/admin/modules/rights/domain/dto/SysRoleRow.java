package com.jorsyframework.starter.admin.modules.rights.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysRoleRow {
    @Schema(description = "角色主键")
    private String sysRoleId;

    @Schema(description = "父级角色主键")
    private String parentId;

    @Schema(description = "角色代码")
    private String sysRoleCode;

    @Schema(description = "角色名称")
    private String sysRoleName;

    @Schema(description = "备注")
    private String remark;

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
    private List<SysRoleRow> children = new ArrayList<>();
}
