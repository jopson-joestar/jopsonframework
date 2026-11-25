package com.jopsonframework.starter.admin.modules.rights.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jopsonframework.starter.core.mybatis.entity.CrudEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(value = "sys_rights_extra")
public class SysRightsExtra extends CrudEntity {
    @TableId(value = "sys_rights_extra_id", type = IdType.ASSIGN_UUID)
    private String sysRightsExtraId;

    private String sysRightsId;

    private String sysOrgId;

    private String sysOrgCode;
}
