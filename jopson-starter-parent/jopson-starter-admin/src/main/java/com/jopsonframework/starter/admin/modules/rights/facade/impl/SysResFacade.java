package com.jopsonframework.starter.admin.modules.rights.facade.impl;

import com.jopsonframework.starter.admin.common.exception.AdminBusinessException;
import com.jopsonframework.starter.admin.modules.rights.access.SysUserOperatorProvider;
import com.jopsonframework.starter.admin.modules.rights.converter.SysResConverter;
import com.jopsonframework.starter.admin.modules.rights.domain.dto.ResourceImportReport;
import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysResRow;
import com.jopsonframework.starter.admin.modules.rights.domain.param.ResourceImport;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysResCreateParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysResUpdateParam;
import com.jopsonframework.starter.admin.modules.rights.facade.ISysResFacade;
import com.jopsonframework.starter.admin.modules.rights.service.ISysResService;
import com.jopsonframework.starter.admin.modules.rights.service.ISysRoleAuthService;
import com.jopsonframework.starter.admin.modules.rights.service.ISysRoleService;
import com.jopsonframework.starter.core.util.TreeUtil;
import java.util.List;
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
public class SysResFacade implements ISysResFacade {
    private final SysUserOperatorProvider access;
    private final ISysResService sysResService;
    private final SysResConverter sysResConverter;
    private final ISysRoleService roles;
    private final ISysRoleAuthService grants;
    private final ResourceImporter importer;

    @Override
    public SysResRow detail(String id) {
        access.require("Admin:SysRes:View");
        return sysResConverter.row(requireFound(sysResService.getById(id)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String create(SysResCreateParam param) {
        String actor = access.require("Admin:SysRes:Create");
        roles.lockHierarchy();
        var row = sysResConverter.create(param);
        importer.validate(row);
        row.setCreateId(actor);
        checkWrite(sysResService.save(row), "新增失败");
        return row.getSysResId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String edit(SysResUpdateParam param) {
        String actor = access.require("Admin:SysRes:Update");
        roles.lockHierarchy();
        var row = sysResService.lockById(param.sysResId());
        checkVersion(row.getVersion(), param.version());
        sysResConverter.edit(param, row);
        importer.validate(row);
        row.setUpdateId(actor);
        checkWrite(sysResService.updateById(row), "记录已变化，请刷新");
        return row.getSysResId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(String id, long version) {
        String actor = access.require("Admin:SysRes:Delete");
        roles.lockHierarchy();
        var row = sysResService.lockById(id);
        checkVersion(row.getVersion(), version);
        if (sysResService.hasChildren(id)) throw AdminBusinessException.conflict("资源仍有子节点");
        grants.removeResource(id);
        sysResService.delete(row, actor);
    }

    @Override
    public List<SysResRow> tree() {
        access.require("Admin:SysRes:View");
        var rows = sysResService.ordered().stream().map(sysResConverter::row).toList();
        return TreeUtil.makeTree(rows, row -> row.getParentId() == null,
                (parent, child) -> parent.getSysResId().equals(child.getParentId()), SysResRow::setChildren);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResourceImportReport importJson(ResourceImport param) {
        String actor = access.require("Admin:SysRes:Import");
        roles.lockHierarchy();
        return importer.run(param, actor);
    }
}
