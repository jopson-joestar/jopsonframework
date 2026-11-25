package com.jorsyframework.starter.admin.modules.rights.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.jorsyframework.starter.core.enums.ValueEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SysResType implements ValueEnum<String> {
    FOLDER("FOLDER", "目录"),
    MENU("MENU", "菜单"),
    BUTTON("BUTTON", "按钮"),
    IFRAME("IFRAME", "内嵌页"),
    LINK("LINK", "外链");

    @EnumValue
    private final String value;
    private final String label;
}
