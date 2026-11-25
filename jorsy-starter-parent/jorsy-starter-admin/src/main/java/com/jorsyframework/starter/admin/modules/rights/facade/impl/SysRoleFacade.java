package com.jorsyframework.starter.admin.modules.rights.facade.impl;

import com.jorsyframework.starter.admin.common.exception.AdminBusinessException;
import com.jorsyframework.starter.admin.modules.rights.access.SysUserOperatorProvider;
import com.jorsyframework.starter.admin.modules.rights.converter.SysRoleConverter;
import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysRoleRow;
import com.jorsyframework.starter.admin.modules.rights.domain.entity.SysRole;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysRoleCreateParam;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysRoleQuery;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysRoleUpdateParam;
import com.jorsyframework.starter.admin.modules.rights.facade.ISysRoleFacade;
import com.jorsyframework.starter.admin.modules.rights.service.ISysRightsService;
import com.jorsyframework.starter.admin.modules.rights.service.ISysRoleAuthService;
import com.jorsyframework.starter.admin.modules.rights.service.ISysRoleService;
import com.jorsyframework.starter.core.util.TreeUtil;
import com.jorsyframework.starter.core.page.PageResult;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.checkVersion;
import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.checkWrite;
import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.requireFound;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SysRoleFacade implements ISysRoleFacade {
    private final SysUserOperatorProvider access;
    private final ISysRoleService sysRoleService;
    private final SysRoleConverter sysRoleConverter;
    private final ISysRightsService identities;
    private final ISysRoleAuthService grants;

    @Override
    public PageResult<SysRoleRow> page(SysRoleQuery param) {
        access.require("Admin:SysRole:View");
        var page = sysRoleService.page(param);
        return new PageResult<>(page.items().stream().map(sysRoleConverter::row).toList(), page.total(), page.currentPage(), page.pageSize());
    }

    @Override
    public SysRoleRow detail(String id) {
        access.require("Admin:SysRole:View");
        return sysRoleConverter.row(requireFound(sysRoleService.getById(id)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String create(SysRoleCreateParam param) {
        String actor = access.require("Admin:SysRole:Create");
        sysRoleService.lockHierarchy();
        var row = sysRoleConverter.create(param);
        if ("administrator".equalsIgnoreCase(row.getSysRoleCode())) throw AdminBusinessException.forbidden("administrator 是保留角色");
        validateParent(null, param.parentId());
        row.setSysData(false);
        row.setCreateId(actor);
        checkWrite(sysRoleService.save(row), "新增失败");
        return row.getSysRoleId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String edit(SysRoleUpdateParam param) {
        String actor = access.require("Admin:SysRole:Update");
        sysRoleService.lockHierarchy();
        var row = sysRoleService.lockById(param.sysRoleId());
        checkVersion(row.getVersion(), param.version());
        protect(row);
        if (!row.getSysRoleCode().equals(param.sysRoleCode()) && identities.usesRole(row.getSysRoleId()))
            throw AdminBusinessException.conflict("已有身份引用的角色不能修改编码");
        if ("administrator".equalsIgnoreCase(param.sysRoleCode())) throw AdminBusinessException.forbidden("administrator 是保留角色");
        SysRole parent = validateParent(row.getSysRoleId(), param.parentId());
        if (!Objects.equals(row.getParentId(), param.parentId())) {
            if (!"administrator".equals(parent.getSysRoleCode()) && !grants.granted(parent.getSysRoleId()).containsAll(grants.granted(row.getSysRoleId())))
                throw AdminBusinessException.conflict("当前角色授权超出新上级范围");
        }
        sysRoleConverter.edit(param, row);
        row.setUpdateId(actor);
        checkWrite(sysRoleService.updateById(row), "记录已变化，请刷新");
        return row.getSysRoleId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(String id, long version) {
        String actor = access.require("Admin:SysRole:Delete");
        sysRoleService.lockHierarchy();
        var row = sysRoleService.lockById(id);
        checkVersion(row.getVersion(), version);
        protect(row);
        if (!sysRoleService.descendants(id).isEmpty() || identities.usesRole(id) || !grants.granted(id).isEmpty())
            throw AdminBusinessException.conflict("角色仍有下级、资源授权或身份引用");
        sysRoleService.delete(row, actor);
    }

    @Override
    public List<SysRoleRow> tree() {
        access.require("Admin:SysRole:View");
        var rows = sysRoleService.ordered().stream().map(sysRoleConverter::row).toList();
        return TreeUtil.makeTree(rows, row -> row.getParentId() == null,
                (parent, child) -> parent.getSysRoleId().equals(child.getParentId()), SysRoleRow::setChildren);
    }

    private SysRole validateParent(String roleId, String parentId) {
        var roles = sysRoleService.ordered().stream().collect(Collectors.toMap(SysRole::getSysRoleId, Function.identity()));
        var visited = new HashSet<String>();
        visited.add(roleId);
        for (String id = parentId; id != null; ) {
            if (!visited.add(id)) throw AdminBusinessException.conflict("父级不能是自身或下级");
            var parent = roles.get(id);
            if (parent == null) throw AdminBusinessException.missing("父级不存在");
            id = parent.getParentId();
        }
        return requireFound(roles.get(parentId));
    }

    private static void protect(SysRole role) {
        if ("administrator".equalsIgnoreCase(role.getSysRoleCode()) || Boolean.TRUE.equals(role.getSysData()))
            throw AdminBusinessException.forbidden("内置角色受保护");
    }
}
