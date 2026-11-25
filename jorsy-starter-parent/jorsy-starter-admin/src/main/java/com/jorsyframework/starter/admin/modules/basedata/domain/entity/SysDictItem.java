package com.jorsyframework.starter.admin.modules.basedata.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.jorsyframework.starter.core.mybatis.entity.CrudEntity;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Getter
@Setter
@TableName(value = "sys_dict_item", autoResultMap = true)
public class SysDictItem extends CrudEntity {
    @TableId(value = "sys_dict_item_id", type = IdType.ASSIGN_UUID)
    private String sysDictItemId;

    private String sysDictId;

    private String dictItemValue;

    private String dictItemLabel;

    private Integer sortNo;

    @TableField(value = "dict_item_extra", typeHandler = Jackson3TypeHandler.class)
    private JsonNode dictItemExtra;

    private String labelColor;

    private String labelClass;

    private Boolean disabled;

    private String remark;
}
