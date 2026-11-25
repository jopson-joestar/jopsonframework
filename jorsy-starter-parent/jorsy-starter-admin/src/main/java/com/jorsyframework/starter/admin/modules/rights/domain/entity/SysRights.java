package com.jorsyframework.starter.admin.modules.rights.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jorsyframework.starter.admin.modules.rights.domain.enums.DataScope;
import com.jorsyframework.starter.core.mybatis.entity.CrudEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(value = "sys_rights")
public class SysRights extends CrudEntity {
    @TableId(value = "sys_rights_id", type = IdType.ASSIGN_UUID)
    private String sysRightsId;

    private String sysUserId;

    private String sysUserCode;

    private String sysOrgId;

    private String sysOrgCode;

    private String sysRoleId;

    private String sysRoleCode;

    private DataScope dataScope;

    private Boolean enabled;

    private Boolean defaulted;

    private Boolean sysData;
}
