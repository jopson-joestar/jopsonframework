package com.jopsonframework.starter.admin.modules.basedata.domain.dto;

import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysDictItem;
import tools.jackson.databind.JsonNode;

public record DictOption(String value, String label, String labelColor, String labelClass, Boolean disabled, JsonNode extra) {
    public static DictOption from(SysDictItem item) {
        return new DictOption(item.getDictItemValue(), item.getDictItemLabel(), item.getLabelColor(),
                item.getLabelClass(), item.getDisabled(), item.getDictItemExtra());
    }
}
