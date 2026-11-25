package com.jopsonframework.starter.admin.modules.rights.facade.impl;

import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysLoginMenu;
import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysLoginUser;
import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysLoginUser.Identity;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysOrg;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRes;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRights;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRightsExtra;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRole;
import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysResType;
import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysUserStatus;
import com.jopsonframework.starter.admin.modules.rights.facade.ISysLoginFacade;
import com.jopsonframework.starter.admin.modules.rights.service.*;
import com.jopsonframework.starter.core.util.TreeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SysLoginFacade implements ISysLoginFacade {
    private final ISysUserService users;
    private final ISysRightsService rights;
    private final ISysOrgService orgs;
    private final ISysRoleService roles;
    private final ISysRoleAuthService grants;
    private final ISysResService resources;
    private final ISysRightsExtraService extras;

    @Override
    public Optional<String> defaultRightsId(String userId) {
        var defaults = rights.enabledForUser(userId).stream()
                .filter(row -> Boolean.TRUE.equals(row.getDefaulted())).toList();
        if (defaults.size() != 1) return Optional.empty();
        var row = defaults.getFirst();
        return orgs.getById(row.getSysOrgId()) != null && roles.getById(row.getSysRoleId()) != null
                ? Optional.of(row.getSysRightsId()) : Optional.empty();
    }

    @Override
    public Optional<SysLoginUser> current(String userId, String rightsId) {
        var user = users.find(userId).filter(row -> row.getSysUserStatus() == SysUserStatus.CHECK);
        if (user.isEmpty()) return Optional.empty();
        var enabled = rights.enabledForUser(userId);
        var selected = enabled.stream().filter(row -> rightsId.equals(row.getSysRightsId())).findFirst();
        if (selected.isEmpty()) return Optional.empty();
        var orgMap = orgs.listByIds(enabled.stream().map(SysRights::getSysOrgId).distinct().toList())
                .stream().collect(Collectors.toMap(SysOrg::getSysOrgId, Function.identity()));
        var roleMap = roles.listByIds(enabled.stream().map(SysRights::getSysRoleId).distinct().toList())
                .stream().collect(Collectors.toMap(SysRole::getSysRoleId, Function.identity()));
        List<Identity> identities = new ArrayList<>();
        for (var row : enabled) {
            var org = orgMap.get(row.getSysOrgId());
            var role = roleMap.get(row.getSysRoleId());
            if (org != null && role != null) {
                identities.add(new Identity(row.getSysRightsId(), org.getSysOrgId(), org.getSysOrgName(),
                        role.getSysRoleId(), role.getSysRoleCode(), role.getSysRoleName(), row.getDataScope()));
            }
        }
        var current = identities.stream().filter(item -> rightsId.equals(item.sysRightsId())).findFirst();
        if (current.isEmpty()) return Optional.empty();
        var role = roleMap.get(current.get().sysRoleId());
        boolean administrator = Boolean.TRUE.equals(selected.get().getSysData())
                && Boolean.TRUE.equals(role.getSysData()) && "administrator".equals(role.getSysRoleCode());
        var granted = administrator ? resources.ordered() : grantedResources(role.getSysRoleId());
        var permissions = granted.stream().map(SysRes::getPermission).filter(Objects::nonNull)
                .filter(code -> !code.isBlank() && !"*".equals(code)).collect(Collectors.toUnmodifiableSet());
        var extraIds = extras.forRights(List.of(rightsId)).stream().map(SysRightsExtra::getSysOrgId).toList();
        return Optional.of(new SysLoginUser(userId, user.get().getSysUserCode(), user.get().getSysUserName(),
                current.get(), identities, permissions, administrator, extraIds));
    }

    private List<SysRes> grantedResources(String roleId) {
        var ids = grants.granted(roleId);
        return ids.isEmpty() ? List.of() : resources.listByIds(ids);
    }

    @Override
    public List<SysLoginMenu> menu(SysLoginUser user) {
        var all = resources.ordered();
        var byId = all.stream().collect(Collectors.toMap(SysRes::getSysResId, Function.identity()));
        var assigned = user.administrator() ? byId.keySet() : grants.granted(user.identity().sysRoleId());
        Set<String> visible = new HashSet<>();
        for (var id : assigned) {
            var row = byId.get(id);
            if (row == null || row.getSysResType() == SysResType.BUTTON) continue;
            while (row != null && visible.add(row.getSysResId())) row = byId.get(row.getParentId());
        }
        var paths = all.stream().filter(row -> visible.contains(row.getSysResId()))
                .map(SysRes::getRoutePath).collect(Collectors.toSet());
        List<SysLoginMenu> nodes = new ArrayList<>();
        for (var row : all) {
            if (!visible.contains(row.getSysResId()) || row.getSysResType() == SysResType.BUTTON) continue;
            var node = new SysLoginMenu();
            node.setId(row.getSysResId());
            node.setParentId(row.getParentId());
            node.setName(row.getRouteName());
            node.setPath(row.getRoutePath());
            if (assigned.contains(row.getSysResId()) || row.getSysResType() == SysResType.FOLDER) {
                node.setMeta(row.getRouteMeta());
            } else if (row.getRouteMeta() instanceof tools.jackson.databind.node.ObjectNode meta) {
                node.setMeta(meta.deepCopy().retain("title", "icon"));
            }
            if (assigned.contains(row.getSysResId())) {
                node.setComponent(row.getComponent());
                if (paths.contains(row.getRedirect())) node.setRedirect(row.getRedirect());
            }
            nodes.add(node);
        }
        return TreeUtil.makeTree(nodes, node -> node.getParentId() == null,
                (parent, child) -> parent.getId().equals(child.getParentId()), SysLoginMenu::setChildren);
    }
}
