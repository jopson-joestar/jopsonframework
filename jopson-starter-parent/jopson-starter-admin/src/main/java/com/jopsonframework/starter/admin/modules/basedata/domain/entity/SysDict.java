package com.jopsonframework.starter.admin.modules.basedata.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jopsonframework.starter.core.mybatis.entity.CrudEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(value = "sys_dict")
public class SysDict extends CrudEntity {
    @TableId(value = "sys_dict_id", type = IdType.ASSIGN_UUID)
    private String sysDictId;

    private String sysDictCode;

    private String sysDictName;

    private Boolean locked;

    private String remark;
}
