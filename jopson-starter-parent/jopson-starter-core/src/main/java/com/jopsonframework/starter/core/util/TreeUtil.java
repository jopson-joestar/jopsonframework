package com.jopsonframework.starter.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class TreeUtil {
    private TreeUtil() {}

    /** 按输入顺序组树，只返回从根节点可达的节点。 */
    public static <T> List<T> makeTree(List<T> rows, Predicate<T> rootCheck,
                                       BiFunction<T, T, Boolean> parentCheck, BiConsumer<T, List<T>> setChildren) {
        List<T> roots = new ArrayList<>();
        for (T row : rows) {
            if (rootCheck.test(row)) {
                setChildren.accept(row, makeChildren(row, rows, parentCheck, setChildren));
                roots.add(row);
            }
        }
        return roots;
    }

    private static <T> List<T> makeChildren(T parent, List<T> rows, BiFunction<T, T, Boolean> parentCheck,
                                            BiConsumer<T, List<T>> setChildren) {
        List<T> children = new ArrayList<>();
        for (T row : rows) {
            if (parentCheck.apply(parent, row)) {
                setChildren.accept(row, makeChildren(row, rows, parentCheck, setChildren));
                children.add(row);
            }
        }
        return children;
    }
}
