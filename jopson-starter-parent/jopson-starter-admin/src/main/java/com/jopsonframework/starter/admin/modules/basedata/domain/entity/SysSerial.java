package com.jopsonframework.starter.admin.modules.basedata.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jopsonframework.starter.core.mybatis.entity.CrudEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(value = "sys_serial")
public class SysSerial extends CrudEntity {
    @TableId(value = "sys_serial_id", type = IdType.ASSIGN_UUID)
    private String sysSerialId;

    private String serialCode;

    private String serialName;

    private String serialPrefix;

    private String serialSuffix;

    private Integer serialDigit;

    private String dateFormat;

    private String lastSerial;

    private Boolean sysData;
}
