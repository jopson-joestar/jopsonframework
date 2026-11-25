package com.jopsonframework.starter.admin.modules.rights.facade.impl;

import com.jopsonframework.starter.admin.common.exception.AdminBusinessException;
import com.jopsonframework.starter.admin.modules.rights.access.SysUserOperatorProvider;
import com.jopsonframework.starter.admin.modules.rights.converter.SysRightsConverter;
import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysRightsRow;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysOrg;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRights;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRightsExtra;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRole;
import com.jopsonframework.starter.admin.modules.rights.domain.param.RightsEnabled;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRightsCreateParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRightsQuery;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRightsUpdateParam;
import com.jopsonframework.starter.admin.modules.rights.facade.ISysRightsFacade;
import com.jopsonframework.starter.admin.modules.rights.service.ISysOrgService;
import com.jopsonframework.starter.admin.modules.rights.service.ISysRightsExtraService;
import com.jopsonframework.starter.admin.modules.rights.service.ISysRightsService;
import com.jopsonframework.starter.admin.modules.rights.service.ISysRoleService;
import com.jopsonframework.starter.admin.modules.rights.service.ISysUserService;
import com.jopsonframework.starter.core.page.PageResult;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.checkVersion;
import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.checkWrite;
import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.requireFound;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SysRightsFacade implements ISysRightsFacade {
    private final SysUserOperatorProvider access;
    private final ISysRightsService sysRightsService;
    private final SysRightsConverter sysRightsConverter;
    private final ISysRoleService roles;
    private final ISysOrgService orgs;
    private final ISysRightsExtraService extras;
    private final ISysUserService users;

    @Override
    public PageResult<SysRightsRow> page(SysRightsQuery param) {
        access.require("Admin:SysRights:View");
        var page = sysRightsService.page(param);
        return new PageResult<>(display(page.items()), page.total(), page.currentPage(), page.pageSize());
    }

    @Override
    public SysRightsRow detail(String id) {
        access.require("Admin:SysRights:View");
        return display(List.of(requireFound(sysRightsService.getById(id)))).getFirst();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String create(SysRightsCreateParam param) {
        String actor = access.require("Admin:SysRights:Create");
        roles.lockHierarchy();
        var row = sysRightsConverter.create(param);
        var selected = resolve(row, param.extraOrgIds());
        row.setSysData(false);
        row.setCreateId(actor);
        checkWrite(sysRightsService.save(row), "新增失败");
        String id = row.getSysRightsId();
        extras.replace(id, selected, actor);
        return id;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String edit(SysRightsUpdateParam param) {
        String actor = access.require("Admin:SysRights:Update");
        roles.lockHierarchy();
        var row = sysRightsService.lockById(param.sysRightsId());
        checkVersion(row.getVersion(), param.version());
        protect(row);
        if (!param.enabled().equals(row.getEnabled())) {
            access.require(enabledPermission(param.enabled()));
        }
        sysRightsConverter.edit(param, row);
        var selected = resolve(row, param.extraOrgIds());
        row.setUpdateId(actor);
        checkWrite(sysRightsService.updateById(row), "记录已变化，请刷新");
        extras.replace(row.getSysRightsId(), selected, actor);
        return row.getSysRightsId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(String id, long version) {
        String actor = access.require("Admin:SysRights:Delete");
        roles.lockHierarchy();
        var row = sysRightsService.lockById(id);
        checkVersion(row.getVersion(), version);
        protect(row);
        extras.replace(id, List.of(), actor);
        sysRightsService.delete(row, actor);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void enabled(RightsEnabled param) {
        String actor = access.require(enabledPermission(param.enabled()));
        roles.lockHierarchy();
        var row = sysRightsService.lockById(param.sysRightsId());
        checkVersion(row.getVersion(), param.version());
        protect(row);
        row.setEnabled(param.enabled());
        row.setUpdateId(actor);
        checkWrite(sysRightsService.updateById(row), "记录已变化，请刷新");
    }

    private static String enabledPermission(boolean enabled) {
        return enabled ? "Admin:SysRights:Enable" : "Admin:SysRights:Disable";
    }

    private static void protect(SysRights row) {
        if (Boolean.TRUE.equals(row.getSysData())) throw AdminBusinessException.forbidden("内置权限身份受保护");
    }

    private List<SysOrg> resolve(SysRights row, List<String> extraIds) {
        var role = roles.lockById(row.getSysRoleId());
        if ("administrator".equalsIgnoreCase(role.getSysRoleCode()))
            throw AdminBusinessException.forbidden("内置管理员身份由部署初始化维护");
        var user = users.find(row.getSysUserId()).orElseThrow(() -> AdminBusinessException.missing("用户不存在"));
        List<String> ids = extraIds == null ? List.of() : extraIds;
        var involved = new java.util.TreeSet<>(ids);
        if (involved.size() != ids.size() || !involved.add(row.getSysOrgId()))
            throw AdminBusinessException.invalid("额外组织不能重复或包含主组织");
        var locked = new HashMap<String, SysOrg>();
        for (String id : involved) locked.put(id, orgs.lockById(id));
        row.setSysOrgCode(locked.get(row.getSysOrgId()).getSysOrgCode());
        row.setSysUserCode(user.getSysUserCode());
        row.setSysRoleCode(role.getSysRoleCode());
        return ids.stream().map(locked::get).toList();
    }

    private List<SysRightsRow> display(List<SysRights> records) {
        if (records.isEmpty()) return List.of();
        var details = extras.forRights(records.stream().map(SysRights::getSysRightsId).toList());
        var grouped = details.stream().collect(Collectors.groupingBy(SysRightsExtra::getSysRightsId));
        var orgIds = records.stream().map(SysRights::getSysOrgId).collect(Collectors.toSet());
        details.forEach(extra -> orgIds.add(extra.getSysOrgId()));
        var organizations = orgs.listByIds(orgIds).stream()
                .collect(Collectors.toMap(SysOrg::getSysOrgId, Function.identity()));
        var roleIds = records.stream().map(SysRights::getSysRoleId).collect(Collectors.toSet());
        var roleRows = roles.listByIds(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getSysRoleId, Function.identity()));
        var names = users.names(records.stream().map(SysRights::getSysUserId).collect(Collectors.toSet()));
        return records.stream().map(record -> {
            var row = sysRightsConverter.row(record);
            var org = organizations.get(record.getSysOrgId());
            var role = roleRows.get(record.getSysRoleId());
            row.setSysUserName(names.get(record.getSysUserId()));
            row.setSysOrgName(org == null ? null : org.getSysOrgName());
            row.setSysRoleName(role == null ? null : role.getSysRoleName());
            var extra = grouped.getOrDefault(record.getSysRightsId(), List.of());
            row.setExtraOrgIds(extra.stream().map(SysRightsExtra::getSysOrgId).toList());
            row.setExtraOrgCodes(extra.stream().map(SysRightsExtra::getSysOrgCode).toList());
            row.setExtraOrgNames(extra.stream().map(item -> organizations.get(item.getSysOrgId()))
                    .map(value -> value == null ? null : value.getSysOrgName()).toList());
            return row;
        }).toList();
    }
}
