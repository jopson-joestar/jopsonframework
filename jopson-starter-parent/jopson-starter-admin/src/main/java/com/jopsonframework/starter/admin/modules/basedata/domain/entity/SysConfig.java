package com.jopsonframework.starter.admin.modules.basedata.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jopsonframework.starter.core.mybatis.entity.CrudEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(value = "sys_config")
public class SysConfig extends CrudEntity {
    @TableId(value = "sys_config_id", type = IdType.ASSIGN_UUID)
    private String sysConfigId;

    private String sysConfigCode;

    private String sysConfigName;

    private String sysConfigValue;

    private String remark;
}
