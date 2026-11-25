package com.jopsonframework.starter.admin.modules.rights.domain.param;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SysUserCreateParam(
        @Schema(description = "登录账号") @NotBlank @Size(max = 50) String sysUserCode,
        @Schema(description = "用户名称") @NotBlank @Size(max = 100) String sysUserName,
        @Schema(description = "初始密码，UTF-8 最多 72 字节", accessMode = Schema.AccessMode.WRITE_ONLY)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NotBlank String initialPassword,
        @Schema(description = "手机号") @Size(max = 30) String sysUserMobile,
        @Schema(description = "邮箱") @Email @Size(max = 100) String sysUserEmail,
        @Schema(description = "QQ 号") @Size(max = 30) String sysUserQq) {

    @JsonIgnore
    @AssertTrue(message = "初始密码最多 72 个 UTF-8 字节")
    public boolean isPasswordLengthValid() {
        return initialPassword == null || initialPassword.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public String toString() {
        return "SysUserCreateParam[sysUserCode=" + sysUserCode + "]";
    }
}
