package com.jorsyframework.starter.admin.modules.rights.domain.param;

import com.jorsyframework.starter.admin.modules.rights.domain.enums.SysResType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

public record SysResCreateParam(
        @Schema(description = "父级主键，根节点为空") @Size(min = 32, max = 32) String parentId,
        @Schema(description = "排序序号") Integer sortNo,
        @Schema(description = "资源类型") @NotNull SysResType sysResType,
        @Schema(description = "权限标识") @Size(max = 100) String permission,
        @Schema(description = "路由名称") @NotBlank @Size(max = 100) String routeName,
        @Schema(description = "路由路径") @Size(max = 500) String routePath,
        @Schema(description = "Vue组件") @Size(max = 500) String component,
        @Schema(description = "路由重定向") @Size(max = 500) String redirect,
        @Schema(description = "Vben路由元数据") JsonNode routeMeta,
        @Schema(description = "备注") @Size(max = 500) String remark) {}
