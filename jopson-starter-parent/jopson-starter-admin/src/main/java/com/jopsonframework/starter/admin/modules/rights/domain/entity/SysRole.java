package com.jopsonframework.starter.admin.modules.rights.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jopsonframework.starter.core.mybatis.entity.CrudEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(value = "sys_role")
public class SysRole extends CrudEntity {
    @TableId(value = "sys_role_id", type = IdType.ASSIGN_UUID)
    private String sysRoleId;

    private String parentId;

    private String sysRoleCode;

    private String sysRoleName;

    private String remark;

    private Boolean sysData;
}
