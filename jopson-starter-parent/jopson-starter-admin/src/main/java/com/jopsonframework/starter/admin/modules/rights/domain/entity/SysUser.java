package com.jopsonframework.starter.admin.modules.rights.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysUserStatus;
import com.jopsonframework.starter.core.mybatis.entity.CrudEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(value = "sys_user", autoResultMap = true)
public class SysUser extends CrudEntity {
    @TableId(value = "sys_user_id", type = IdType.ASSIGN_UUID)
    private String sysUserId;

    @TableField(value = "sys_user_code", updateStrategy = FieldStrategy.NEVER)
    private String sysUserCode;

    @JsonIgnore
    @TableField(value = "sys_user_pwd", select = false, updateStrategy = FieldStrategy.NEVER)
    private String sysUserPwd;

    @TableField("sys_user_name")
    private String sysUserName;

    @TableField("sys_user_mobile")
    private String sysUserMobile;

    @TableField("sys_user_email")
    private String sysUserEmail;

    @TableField("sys_user_qq")
    private String sysUserQq;

    @TableField("avatar_file_id")
    private String avatarFileId;

    @TableField("sys_user_status")
    private SysUserStatus sysUserStatus;

    @TableField("sys_data")
    private Boolean sysData;
}
