package com.jorsyframework.starter.admin.modules.rights.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(value = "sys_role_auth")
public class SysRoleAuth {
    @TableId(value = "sys_role_auth_id", type = IdType.ASSIGN_UUID)
    private String sysRoleAuthId;

    private String sysRoleId;

    private String sysResId;
}
