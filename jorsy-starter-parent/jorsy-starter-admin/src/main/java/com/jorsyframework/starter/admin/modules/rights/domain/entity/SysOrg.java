package com.jorsyframework.starter.admin.modules.rights.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jorsyframework.starter.admin.modules.rights.domain.enums.SysOrgType;
import com.jorsyframework.starter.core.mybatis.entity.CrudEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(value = "sys_org")
public class SysOrg extends CrudEntity {
    @TableId(value = "sys_org_id", type = IdType.ASSIGN_UUID)
    private String sysOrgId;

    private String parentId;

    private String sysOrgCode;

    private String sysOrgName;

    private SysOrgType sysOrgType;

    private Integer sortNo;

    private Boolean sysData;
}
