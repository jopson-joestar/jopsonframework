package com.jopsonframework.starter.admin.modules.rights.facade.impl;

import com.jopsonframework.starter.admin.common.exception.AdminBusinessException;
import com.jopsonframework.starter.admin.modules.rights.access.SysUserOperatorProvider;
import com.jopsonframework.starter.admin.modules.rights.converter.SysResConverter;
import com.jopsonframework.starter.admin.modules.rights.domain.dto.RoleResourceTree;
import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysResRow;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRes;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRole;
import com.jopsonframework.starter.admin.modules.rights.domain.param.RoleResources;
import com.jopsonframework.starter.admin.modules.rights.facade.ISysRoleAuthFacade;
import com.jopsonframework.starter.admin.modules.rights.service.ISysResService;
import com.jopsonframework.starter.admin.modules.rights.service.ISysRoleAuthService;
import com.jopsonframework.starter.admin.modules.rights.service.ISysRoleService;
import com.jopsonframework.starter.core.util.TreeUtil;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
public class SysRoleAuthFacade implements ISysRoleAuthFacade {
    private final SysUserOperatorProvider access;
    private final ISysRoleService roles;
    private final ISysResService resources;
    private final ISysRoleAuthService grants;
    private final SysResConverter sysResConverter;

    @Override
    public RoleResourceTree list(String sysRoleId) {
        access.require("Admin:SysRoleAuth:View");
        var role = requireFound(roles.getById(sysRoleId));
        var all = resources.ordered();
        var available = permitted(role, all);
        var nodes = all.stream().filter(resource -> available.contains(resource.getSysResId())).map(sysResConverter::row).toList();
        var tree = TreeUtil.makeTree(nodes, row -> row.getParentId() == null,
                (parent, child) -> parent.getSysResId().equals(child.getParentId()), SysResRow::setChildren);
        var checked = administrator(role) ? available : grants.granted(sysRoleId);
        return new RoleResourceTree(tree, checked, available, role.getVersion());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void authorize(RoleResources param) {
        String actor = access.require("Admin:SysRoleAuth:AuthRes");
        roles.lockHierarchy();
        var role = roles.lockById(param.sysRoleId());
        checkVersion(role.getVersion(), param.version());
        if (administrator(role)) throw AdminBusinessException.forbidden("内置管理员角色不能分配资源");
        Set<String> requested = new LinkedHashSet<>(param.sysResIds());
        if (requested.size() != param.sysResIds().size()) throw AdminBusinessException.invalid("资源主键重复");
        if (!permitted(role, resources.ordered()).containsAll(requested))
            throw AdminBusinessException.forbidden("资源不存在或超出父角色授权范围");
        Set<String> existing = grants.granted(param.sysRoleId());
        Set<String> removed = new HashSet<>(existing);
        removed.removeAll(requested);
        if (grants.hasAny(roles.descendants(param.sysRoleId()), removed))
            throw AdminBusinessException.conflict("下级角色仍持有待撤销资源");
        grants.replace(param.sysRoleId(), requested, existing);
        role.setUpdateId(actor);
        checkWrite(roles.updateById(role), "记录已变化，请刷新");
    }

    private Set<String> permitted(SysRole role, List<SysRes> resources) {
        if (administrator(role) || administrator(requireFound(roles.getById(role.getParentId())))) {
            return resources.stream().map(SysRes::getSysResId).collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return grants.granted(role.getParentId());
    }

    private boolean administrator(SysRole role) {
        return Boolean.TRUE.equals(role.getSysData()) && "administrator".equals(role.getSysRoleCode());
    }
}
