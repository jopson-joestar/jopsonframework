package com.jorsyframework.starter.admin.modules.rights.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.jorsyframework.starter.core.enums.ValueEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SysOrgType implements ValueEnum<String> {
    COMPANY("C", "公司"),
    DEPARTMENT("D", "部门"),
    GROUP("G", "小组");

    @EnumValue
    private final String value;
    private final String label;
}
