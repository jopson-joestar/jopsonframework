package com.jopsonframework.starter.admin.modules.basedata.facade.impl;

import com.jopsonframework.starter.admin.common.exception.AdminBusinessException;
import com.jopsonframework.starter.admin.modules.basedata.converter.SysConfigConverter;
import com.jopsonframework.starter.admin.modules.basedata.domain.dto.SysConfigRow;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysConfigCreateParam;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysConfigQuery;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysConfigUpdateParam;
import com.jopsonframework.starter.admin.modules.basedata.facade.ISysConfigFacade;
import com.jopsonframework.starter.admin.modules.basedata.service.ISysConfigService;
import com.jopsonframework.starter.admin.modules.rights.access.SysUserOperatorProvider;
import com.jopsonframework.starter.core.page.PageResult;
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
public class SysConfigFacade implements ISysConfigFacade {
    private final SysUserOperatorProvider access;
    private final ISysConfigService sysConfigService;
    private final SysConfigConverter sysConfigConverter;

    @Override
    public PageResult<SysConfigRow> page(SysConfigQuery param) {
        access.require("Admin:SysConfig:View");
        var page = sysConfigService.page(param);
        return new PageResult<>(page.items().stream().map(sysConfigConverter::row).toList(), page.total(), page.currentPage(), page.pageSize());
    }

    @Override
    public SysConfigRow detail(String id) {
        access.require("Admin:SysConfig:View");
        return sysConfigConverter.row(requireFound(sysConfigService.getById(id)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String create(SysConfigCreateParam param) {
        String actor = access.require("Admin:SysConfig:Create");
        var row = sysConfigConverter.create(param);
        row.setCreateId(actor);
        checkWrite(sysConfigService.save(row), "新增失败");
        return row.getSysConfigId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String edit(SysConfigUpdateParam param) {
        String actor = access.require("Admin:SysConfig:Update");
        var row = sysConfigService.lockById(param.sysConfigId());
        checkVersion(row.getVersion(), param.version());
        sysConfigConverter.edit(param, row);
        row.setUpdateId(actor);
        checkWrite(sysConfigService.updateById(row), "记录已变化，请刷新");
        return row.getSysConfigId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(String id, long version) {
        String actor = access.require("Admin:SysConfig:Delete");
        var row = sysConfigService.lockById(id);
        checkVersion(row.getVersion(), version);
        sysConfigService.delete(row, actor);
    }

    @Override
    public String value(String sysConfigCode) {
        access.require("Admin:SysConfig:View");
        var row = sysConfigService.byCode(sysConfigCode);
        if (row == null) throw AdminBusinessException.missing("配置不存在");
        return row.getSysConfigValue();
    }
}
