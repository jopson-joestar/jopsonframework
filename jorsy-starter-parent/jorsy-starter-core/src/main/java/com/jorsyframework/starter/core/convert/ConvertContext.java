package com.jorsyframework.starter.core.convert;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;

/** 一页数据的一次批量查询结果，可作为 MapStruct 的 @Context 参数。 */
public final class ConvertContext<K, V> {
    private final Map<K, V> values;

    private ConvertContext(Map<K, V> values) {
        this.values = new HashMap<>(values);
    }

    public static <S, K, V> ConvertContext<K, V> load(Collection<S> rows,
            Function<? super S, ? extends K> keyReader, ConvertProvider<K, V> provider) {
        var keys = new LinkedHashSet<K>();
        for (S row : rows) {
            K key = keyReader.apply(row);
            if (key != null && !(key instanceof String text && text.isBlank())) {
                keys.add(key);
            }
        }
        return new ConvertContext<>(keys.isEmpty() ? Map.of() : provider.getAll(Collections.unmodifiableSet(keys)));
    }

    public V get(K key) {
        return values.get(key);
    }
}
