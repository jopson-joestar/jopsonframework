package com.jopsonframework.starter.admin.modules.rights.domain.dto;

import java.time.LocalDateTime;

import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysUserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "系统用户资料与最后一次写入的审计信息")
public record SysUserRow(
        @Schema(description = "用户主键") String sysUserId,
        @Schema(description = "登录账号") String sysUserCode,
        @Schema(description = "用户名称") String sysUserName,
        @Schema(description = "手机号") String sysUserMobile,
        @Schema(description = "邮箱") String sysUserEmail,
        @Schema(description = "QQ 号") String sysUserQq,
        @Schema(description = "头像文件 ID，只读") String avatarFileId,
        @Schema(description = "用户状态编码") SysUserStatus sysUserStatus,
        @Schema(description = "内置账号标志") Boolean sysData,
        @Schema(description = "创建人 ID") String createId,
        @Schema(description = "创建人名称") String createName,
        @Schema(description = "创建时间") LocalDateTime createTime,
        @Schema(description = "修改人 ID") String updateId,
        @Schema(description = "修改人名称") String updateName,
        @Schema(description = "修改时间") LocalDateTime updateTime,
        @Schema(description = "下一次编辑、状态变更或删除须提交此版本") Long version) {}
