package com.jorsyframework.starter.admin.modules.rights.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.jorsyframework.starter.admin.modules.rights.domain.enums.SysResType;
import com.jorsyframework.starter.core.mybatis.entity.CrudEntity;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Getter
@Setter
@TableName(value = "sys_res", autoResultMap = true)
public class SysRes extends CrudEntity {
    @TableId(value = "sys_res_id", type = IdType.ASSIGN_UUID)
    private String sysResId;

    private String parentId;

    private Integer sortNo;

    private SysResType sysResType;

    private String permission;

    private String routeName;

    private String routePath;

    private String component;

    private String redirect;

    @TableField(value = "route_meta", typeHandler = Jackson3TypeHandler.class)
    private JsonNode routeMeta;

    private String remark;
}
