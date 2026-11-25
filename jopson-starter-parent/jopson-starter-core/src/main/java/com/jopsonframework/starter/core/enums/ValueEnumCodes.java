package com.jopsonframework.starter.core.enums;

import java.util.HashMap;
import java.util.Map;

public final class ValueEnumCodes {
    private ValueEnumCodes() {}

    private static final ClassValue<Map<String, ValueEnum<?>>> CODES = new ClassValue<>() {
        @Override
        protected Map<String, ValueEnum<?>> computeValue(Class<?> type) {
            Map<String, ValueEnum<?>> codes = new HashMap<>();
            Class<?> codeType = null;
            for (Object constant : type.getEnumConstants()) {
                var entry = (ValueEnum<?>) constant;
                Object code = entry.getValue();
                if (!(code instanceof Integer) && !(code instanceof String text && !text.isBlank())) {
                    throw new IllegalStateException(type.getName() + " 的编码必须为整数或非空字符串");
                }
                if (codeType != null && codeType != code.getClass()) {
                    throw new IllegalStateException(type.getName() + " 的编码类型必须一致");
                }
                codeType = code.getClass();
                if (codes.putIfAbsent(code.toString(), entry) != null) {
                    throw new IllegalStateException(type.getName() + " 存在重复编码 " + code);
                }
            }
            return Map.copyOf(codes);
        }
    };

    static void validate(Class<?> type) {
        CODES.get(type);
    }

    /** 根据 URL 文本精确查找，未声明的编码返回 null。 */
    public static ValueEnum<?> find(Class<?> type, String text) {
        return CODES.get(type).get(text);
    }
}
