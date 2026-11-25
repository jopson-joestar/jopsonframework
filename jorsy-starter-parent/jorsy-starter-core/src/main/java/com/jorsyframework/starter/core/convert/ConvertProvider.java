package com.jorsyframework.starter.core.convert;

import java.util.Map;
import java.util.Set;

/** 按调用方的数据权限批量查找，未找到的键可以省略。 */
@FunctionalInterface
public interface ConvertProvider<K, V> {
    Map<K, V> getAll(Set<K> keys);
}
