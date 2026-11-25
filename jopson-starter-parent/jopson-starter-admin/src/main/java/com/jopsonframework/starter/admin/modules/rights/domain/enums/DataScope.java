package com.jopsonframework.starter.admin.modules.rights.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.jopsonframework.starter.core.enums.ValueEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DataScope implements ValueEnum<String> {
    USER_ONLY("1", "仅本人"),
    ORG_ONLY("2", "本组织"),
    ORG_CHILD("3", "本组织及下级"),
    ALL("9", "全部");

    @EnumValue
    private final String value;
    private final String label;
}
