package com.jopsonframework.starter.admin.modules.rights.domain.dto;

import com.jopsonframework.starter.admin.modules.rights.domain.enums.DataScope;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysRightsRow {
    @Schema(description = "权限身份主键")
    private String sysRightsId;

    @Schema(description = "用户主键")
    private String sysUserId;

    @Schema(description = "用户账号")
    private String sysUserCode;

    @Schema(description = "组织主键")
    private String sysOrgId;

    @Schema(description = "组织编码")
    private String sysOrgCode;

    @Schema(description = "角色主键")
    private String sysRoleId;

    @Schema(description = "角色编码")
    private String sysRoleCode;

    @Schema(description = "基础数据权限：1仅本人 2本组织 3本组织及下级 9全部")
    private DataScope dataScope;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "是否默认")
    private Boolean defaulted;

    @Schema(description = "系统数据")
    private Boolean sysData;

    @Schema(description = "创建人主键")
    private String createId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改人主键")
    private String updateId;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;

    @Schema(description = "版本号")
    private Long version;

    @Schema(description = "额外组织主键")
    private List<String> extraOrgIds;

    @Schema(description = "额外组织编码")
    private List<String> extraOrgCodes;

    @Schema(description = "额外组织名称")
    private List<String> extraOrgNames;

    @Schema(description = "用户名称")
    private String sysUserName;

    @Schema(description = "组织名称")
    private String sysOrgName;

    @Schema(description = "角色名称")
    private String sysRoleName;
}
