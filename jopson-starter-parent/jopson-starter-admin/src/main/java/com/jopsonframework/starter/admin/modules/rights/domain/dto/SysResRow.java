package com.jopsonframework.starter.admin.modules.rights.domain.dto;

import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysResType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Getter
@Setter
public class SysResRow {
    @Schema(description = "资源主键")
    private String sysResId;

    @Schema(description = "父级主键，根节点为空")
    private String parentId;

    @Schema(description = "排序序号")
    private Integer sortNo;

    @Schema(description = "资源类型")
    private SysResType sysResType;

    @Schema(description = "权限标识")
    private String permission;

    @Schema(description = "路由名称")
    private String routeName;

    @Schema(description = "路由路径")
    private String routePath;

    @Schema(description = "Vue组件")
    private String component;

    @Schema(description = "路由重定向")
    private String redirect;

    @Schema(description = "Vben路由元数据")
    private JsonNode routeMeta;

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

    @Schema(description = "下级节点")
    private List<SysResRow> children = new ArrayList<>();
}
