package com.jopsonframework.starter.admin.modules.basedata.facade.impl;

import com.jopsonframework.starter.admin.common.enums.SyncOperation;
import com.jopsonframework.starter.admin.modules.basedata.domain.dto.DictSyncReport;
import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysDict;
import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysDictItem;
import com.jopsonframework.starter.admin.modules.basedata.service.ISysDictItemService;
import com.jopsonframework.starter.admin.modules.basedata.service.ISysDictService;
import com.jopsonframework.starter.admin.modules.rights.domain.enums.DataScope;
import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysOrgType;
import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysResType;
import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysUserStatus;
import com.jopsonframework.starter.core.enums.ValueEnum;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.checkWrite;

@Component
@RequiredArgsConstructor
public class DictionarySync {
    private final ISysDictService dictionaries;
    private final ISysDictItemService items;

    private record Entry(String value, String label) {}
    private record Definition(String code, String name, List<Entry> entries) {}

    private static <E extends Enum<E> & ValueEnum<?>> Definition definition(
            String code, String name, E[] values, Function<E, String> label) {
        return new Definition(code, name, java.util.Arrays.stream(values)
                .map(e -> new Entry(e.getValue().toString(), label.apply(e))).toList());
    }

    private static final List<Definition> REGISTERED = List.of(
            definition("admin_sys_user_status", "用户状态", SysUserStatus.values(), SysUserStatus::getLabel),
            definition("admin_sys_org_type", "组织类型", SysOrgType.values(), SysOrgType::getLabel),
            definition("admin_sys_res_type", "资源类型", SysResType.values(), SysResType::getLabel),
            definition("data_scope", "数据范围", DataScope.values(), DataScope::getLabel));

    DictSyncReport run(Set<SyncOperation> operations, String actor) {
        int created = 0, updated = 0, insertedItems = 0, updatedItems = 0, deletedItems = 0, unchanged = 0;
        for (Definition definition : REGISTERED) {
            var dict = dictionaries.lockCode(definition.code());
            if (dict == null) {
                if (!operations.contains(SyncOperation.ADD)) continue;
                dict = new SysDict();
                dict.setSysDictCode(definition.code());
                dict.setSysDictName(definition.name());
                dict.setLocked(true);
                dict.setCreateId(actor);
                checkWrite(dictionaries.save(dict), "新增失败");
                created++;
            } else if (operations.contains(SyncOperation.UPDATE)
                    && (!Boolean.TRUE.equals(dict.getLocked()) || !definition.name().equals(dict.getSysDictName()))) {
                dict.setLocked(true);
                dict.setSysDictName(definition.name());
                dict.setUpdateId(actor);
                checkWrite(dictionaries.updateById(dict), "记录已变化，请刷新");
                updated++;
            }
            var remaining = items.forDict(dict.getSysDictId()).stream()
                    .collect(Collectors.toMap(SysDictItem::getDictItemValue, Function.identity()));
            int sort = 0;
            for (var entry : definition.entries()) {
                sort++;
                var item = remaining.remove(entry.value());
                if (item == null) {
                    if (!operations.contains(SyncOperation.ADD)) continue;
                    item = new SysDictItem();
                    item.setSysDictId(dict.getSysDictId());
                    item.setDictItemValue(entry.value());
                    item.setDictItemLabel(entry.label());
                    item.setSortNo(sort);
                    item.setDisabled(false);
                    item.setCreateId(actor);
                    checkWrite(items.save(item), "新增失败");
                    insertedItems++;
                } else if (operations.contains(SyncOperation.UPDATE)
                        && (!entry.label().equals(item.getDictItemLabel()) || !Objects.equals(sort, item.getSortNo()))) {
                    item.setDictItemLabel(entry.label());
                    item.setSortNo(sort);
                    item.setUpdateId(actor);
                    checkWrite(items.updateById(item), "记录已变化，请刷新");
                    updatedItems++;
                } else {
                    unchanged++;
                }
            }
            if (operations.contains(SyncOperation.DELETE)) {
                for (var stale : remaining.values()) items.delete(stale, actor);
                deletedItems += remaining.size();
            }
        }
        return new DictSyncReport(REGISTERED.size(), created, updated, insertedItems, updatedItems, deletedItems, unchanged);
    }
}
