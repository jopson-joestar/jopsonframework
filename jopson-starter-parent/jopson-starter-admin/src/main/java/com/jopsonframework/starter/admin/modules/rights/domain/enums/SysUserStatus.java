package com.jopsonframework.starter.admin.modules.rights.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.jopsonframework.starter.core.enums.ValueEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SysUserStatus implements ValueEnum<Integer> {
    VOID(-100, "作废"),
    CREATE(0, "未审核"),
    LOCK(1, "锁定"),
    CHECK(10, "审核通过");

    @EnumValue
    private final Integer value;
    private final String label;
}
