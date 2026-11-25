package com.jorsyframework.starter.admin.modules.rights.facade.impl;

import com.jorsyframework.starter.admin.common.exception.AdminBusinessException;
import com.jorsyframework.starter.admin.modules.rights.access.SysUserOperatorProvider;
import com.jorsyframework.starter.admin.modules.rights.converter.SysOrgConverter;
import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysOrgRow;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysOrgCreateParam;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysOrgUpdateParam;
import com.jorsyframework.starter.admin.modules.rights.facade.ISysOrgFacade;
import com.jorsyframework.starter.admin.modules.rights.service.ISysOrgService;
import com.jorsyframework.starter.admin.modules.rights.service.ISysRightsExtraService;
import com.jorsyframework.starter.admin.modules.rights.service.ISysRightsService;
import com.jorsyframework.starter.core.util.TreeUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class SysOrgFacade implements ISysOrgFacade {
    private final SysUserOperatorProvider access;
    private final ISysOrgService sysOrgService;
    private final SysOrgConverter sysOrgConverter;
    private final ISysRightsService identities;
    private final ISysRightsExtraService extras;

    @Override
    public SysOrgRow detail(String id) {
        access.require("Admin:SysOrg:View");
        return sysOrgConverter.row(requireFound(sysOrgService.getById(id)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String create(SysOrgCreateParam param) {
        String actor = access.require("Admin:SysOrg:Create");
        var row = sysOrgConverter.create(param);
        row.setSysOrgCode(sysOrgService.nextCode(param.parentId()));
        row.setSysData(false);
        row.setCreateId(actor);
        checkWrite(sysOrgService.save(row), "新增失败");
        return row.getSysOrgId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String edit(SysOrgUpdateParam param) {
        String actor = access.require("Admin:SysOrg:Update");
        var row = sysOrgService.lockById(param.sysOrgId());
        checkVersion(row.getVersion(), param.version());
        sysOrgConverter.edit(param, row);
        row.setUpdateId(actor);
        checkWrite(sysOrgService.updateById(row), "记录已变化，请刷新");
        return row.getSysOrgId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(String id, long version) {
        String actor = access.require("Admin:SysOrg:Delete");
        var row = sysOrgService.lockById(id);
        checkVersion(row.getVersion(), version);
        if (row.getParentId() == null || Boolean.TRUE.equals(row.getSysData())) throw AdminBusinessException.conflict("根组织和内置组织受保护");
        if (sysOrgService.hasChildren(id) || identities.usesOrg(id) || extras.usesOrg(id)) throw AdminBusinessException.conflict("组织仍有下级或权限身份引用");
        sysOrgService.delete(row, actor);
    }

    @Override
    public List<SysOrgRow> tree() {
        access.require("Admin:SysOrg:View");
        var rows = sysOrgService.ordered().stream().map(sysOrgConverter::row).toList();
        return TreeUtil.makeTree(rows, row -> row.getParentId() == null,
                (parent, child) -> parent.getSysOrgId().equals(child.getParentId()), SysOrgRow::setChildren);
    }

    @Override
    public Map<String, String> names(List<String> codes) {
        access.require("Admin:SysOrg:View");
        var names = new LinkedHashMap<String, String>();
        sysOrgService.byCodes(codes).forEach(org -> names.put(org.getSysOrgCode(), org.getSysOrgName()));
        return names;
    }

    @Override
    public String name(String sysOrgCode) {
        access.require("Admin:SysOrg:View");
        var rows = sysOrgService.byCodes(List.of(sysOrgCode));
        if (rows.isEmpty()) throw AdminBusinessException.missing("组织不存在");
        return rows.getFirst().getSysOrgName();
    }
}
