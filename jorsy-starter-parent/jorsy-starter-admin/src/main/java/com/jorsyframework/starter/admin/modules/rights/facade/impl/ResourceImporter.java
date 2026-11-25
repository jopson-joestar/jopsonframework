package com.jorsyframework.starter.admin.modules.rights.facade.impl;

import com.jorsyframework.starter.admin.common.enums.SyncOperation;
import com.jorsyframework.starter.admin.common.exception.AdminBusinessException;
import com.jorsyframework.starter.admin.modules.rights.converter.SysResConverter;
import com.jorsyframework.starter.admin.modules.rights.domain.dto.ResourceImportReport;
import com.jorsyframework.starter.admin.modules.rights.domain.entity.SysRes;
import com.jorsyframework.starter.admin.modules.rights.domain.enums.SysResType;
import com.jorsyframework.starter.admin.modules.rights.domain.param.ResourceImport;
import com.jorsyframework.starter.admin.modules.rights.service.ISysResService;
import com.jorsyframework.starter.admin.modules.rights.service.ISysRoleAuthService;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;

import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.checkWrite;

@Component
@RequiredArgsConstructor
public class ResourceImporter {
    private static final Pattern NAME = Pattern.compile("[A-Z][A-Za-z0-9]*");
    private static final Pattern PERMISSION = Pattern.compile("Admin:[A-Z][A-Za-z0-9]*:[A-Z][A-Za-z0-9]*");
    private static final Pattern PATH = Pattern.compile("/(?:[a-z][a-z0-9-]*|:[a-z][A-Za-z0-9]*\\??)(?:/(?:[a-z][a-z0-9-]*|:[a-z][A-Za-z0-9]*\\??))*");
    private final ISysResService resources;
    private final ISysRoleAuthService grants;
    private final SysResConverter convert;

    void validate(SysRes row) {
        validateFields(row);
        if (row.getParentId() != null) {
            var byId = resources.ordered().stream().collect(Collectors.toMap(SysRes::getSysResId, Function.identity()));
            var visited = new HashSet<String>();
            visited.add(row.getSysResId());
            for (String id = row.getParentId(); id != null; ) {
                if (!visited.add(id)) throw AdminBusinessException.conflict("父级不能是自身或下级");
                var parent = byId.get(id);
                if (parent == null) throw AdminBusinessException.missing("父级不存在");
                if (!canHaveChildren(parent)) throw AdminBusinessException.invalid("所选父资源不支持子节点");
                id = parent.getParentId();
            }
        }
        if (row.getSysResId() != null && !canHaveChildren(row) && resources.hasChildren(row.getSysResId()))
            throw AdminBusinessException.conflict("含子节点的资源不能改成叶节点");
    }

    ResourceImportReport run(ResourceImport request, String actor) {
        boolean full = request.mode() == ResourceImport.Mode.FULL;
        Set<SyncOperation> actions = request.operations() == null ? Set.of() : request.operations();
        if (full && !actions.isEmpty() || !full && actions.isEmpty())
            throw AdminBusinessException.invalid("全量导入无需选择动作，增量导入须选择动作");
        var old = resources.ordered().stream().collect(Collectors.toMap(SysRes::getRouteName, Function.identity()));
        int deleted = 0;
        if (full) {
            for (var row : old.values()) remove(row, actor);
            deleted = old.size();
        }
        var seen = new HashSet<String>();
        int added = 0, updated = 0;
        record Pending(ResourceImport.Node node, String parentId, boolean missingParent) {}
        var pending = new ArrayDeque<Pending>();
        request.resources().forEach(node -> pending.addLast(new Pending(node, null, false)));
        while (!pending.isEmpty()) {
            var next = pending.removeFirst();
            var node = next.node();
            if (!seen.add(node.routeName())) throw AdminBusinessException.invalid("导入资源的路由名重复");
            var candidate = convert.create(node.form(next.parentId()));
            validateFields(candidate);
            var previous = old.get(node.routeName());
            if (candidate.getRemark() == null && previous != null) candidate.setRemark(previous.getRemark());
            var row = full ? null : previous;
            boolean insert = row == null && (full || actions.contains(SyncOperation.ADD));
            boolean update = row != null && actions.contains(SyncOperation.UPDATE);
            if ((insert || update) && next.missingParent()) throw AdminBusinessException.conflict("请先添加缺失的父资源");
            String id = row == null ? null : row.getSysResId();
            if (insert) {
                candidate.setCreateId(actor);
                checkWrite(resources.save(candidate), "新增失败");
                id = candidate.getSysResId();
                added++;
            } else if (update) {
                candidate.setSysResId(id);
                candidate.setVersion(row.getVersion());
                candidate.setUpdateId(actor);
                checkWrite(resources.updateById(candidate), "记录已变化，请刷新");
                updated++;
            }
            if (node.children() != null) {
                if (!node.children().isEmpty() && !canHaveChildren(candidate)) throw AdminBusinessException.invalid("叶节点不能包含子资源");
                for (var child : node.children()) pending.addLast(new Pending(child, id, id == null));
            }
        }
        if (!full && actions.contains(SyncOperation.DELETE)) {
            for (var row : old.values()) {
                if (!seen.contains(row.getRouteName())) {
                    remove(row, actor);
                    deleted++;
                }
            }
        }
        validateImportedTree(resources.ordered());
        return new ResourceImportReport(added, updated, deleted);
    }

    private static void validateImportedTree(List<SysRes> rows) {
        var pending = new ArrayDeque<SysRes>();
        Map<String, List<SysRes>> children = new HashMap<>();
        for (var row : rows) {
            if (row.getParentId() == null) pending.add(row);
            else children.computeIfAbsent(row.getParentId(), ignored -> new ArrayList<>()).add(row);
        }
        int visited = 0;
        while (!pending.isEmpty()) {
            var parent = pending.removeFirst();
            visited++;
            var branch = children.get(parent.getSysResId());
            if (branch == null) continue;
            if (!canHaveChildren(parent)) throw AdminBusinessException.conflict("导入后父级类型不允许包含子资源");
            pending.addAll(branch);
        }
        if (visited != rows.size()) throw AdminBusinessException.conflict("导入结果存在无效父节点或循环引用");
    }

    private void remove(SysRes row, String actor) {
        grants.removeResource(row.getSysResId());
        resources.delete(row, actor);
    }

    private static boolean canHaveChildren(SysRes row) {
        return canHaveChildren(row.getSysResType());
    }

    private static boolean canHaveChildren(SysResType type) {
        return type == SysResType.MENU || type == SysResType.FOLDER;
    }

    private static void validateFields(SysRes row) {
        if (row.getRouteName() == null || !NAME.matcher(row.getRouteName()).matches())
            throw AdminBusinessException.invalid("路由名必须为 PascalCase");
        String permission = row.getPermission();
        if (permission != null && !permission.isBlank() && !PERMISSION.matcher(permission).matches())
            throw AdminBusinessException.invalid("权限标识须符合 Admin:Resource:Action");
        if (!(row.getRouteMeta() instanceof ObjectNode meta) || meta.path("title").asString("").isBlank())
            throw AdminBusinessException.invalid("资源标题不能为空");
        if (row.getSysResType() != SysResType.BUTTON
                && (row.getRoutePath() == null || !PATH.matcher(row.getRoutePath()).matches()))
            throw AdminBusinessException.invalid("路由必须为绝对路径");
        if (!meta.path("affixTab").asBoolean(false)) meta.remove("affixTabOrder");
        switch (row.getSysResType()) {
            case BUTTON -> {
                if (permission == null || permission.isBlank()) throw AdminBusinessException.invalid("按钮须指定权限码");
                row.setRoutePath(null);
                row.setComponent(null);
                row.setRedirect(null);
                meta.retain("title");
            }
            case MENU -> {
                if (row.getComponent() == null || row.getComponent().isBlank()) throw AdminBusinessException.invalid("菜单须指定组件");
                meta.remove("iframeSrc");
                meta.remove("link");
            }
            case FOLDER -> {
                row.setComponent(null);
                meta.remove("link");
                meta.remove("iframeSrc");
            }
            case IFRAME -> {
                checkUrl(meta.path("iframeSrc").asString(""));
                if (row.getComponent() == null || row.getComponent().isBlank()) row.setComponent("IFrameView");
                meta.remove("link");
            }
            case LINK -> {
                checkUrl(meta.path("link").asString(""));
                row.setComponent(null);
                meta.remove("iframeSrc");
            }
        }
    }

    private static void checkUrl(String url) {
        try {
            var uri = URI.create(url);
            if (uri.getHost() != null && ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))) return;
        } catch (IllegalArgumentException ignored) {
            // Return the same validation response for malformed and unsupported URLs.
        }
        throw AdminBusinessException.invalid("外部地址须为有效的 HTTP 或 HTTPS URL");
    }
}
