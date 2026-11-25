package com.jorsyframework.starter.admin.modules.basedata.facade.impl;

import com.jorsyframework.starter.admin.common.exception.AdminBusinessException;
import com.jorsyframework.starter.admin.modules.basedata.converter.SysDictItemConverter;
import com.jorsyframework.starter.admin.modules.basedata.domain.dto.SysDictItemRow;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictItemCreateParam;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictItemQuery;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictItemUpdateParam;
import com.jorsyframework.starter.admin.modules.basedata.facade.ISysDictItemFacade;
import com.jorsyframework.starter.admin.modules.basedata.service.ISysDictItemService;
import com.jorsyframework.starter.admin.modules.basedata.service.ISysDictService;
import com.jorsyframework.starter.admin.modules.rights.access.SysUserOperatorProvider;
import com.jorsyframework.starter.core.page.PageResult;
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
public class SysDictItemFacade implements ISysDictItemFacade {
    private final SysUserOperatorProvider access;
    private final ISysDictItemService sysDictItemService;
    private final SysDictItemConverter sysDictItemConverter;
    private final ISysDictService dictionaries;

    @Override
    public PageResult<SysDictItemRow> page(SysDictItemQuery param) {
        access.require("Admin:SysDictItem:View");
        var page = sysDictItemService.page(param);
        return new PageResult<>(page.items().stream().map(sysDictItemConverter::row).toList(), page.total(), page.currentPage(), page.pageSize());
    }

    @Override
    public SysDictItemRow detail(String id) {
        access.require("Admin:SysDictItem:View");
        return sysDictItemConverter.row(requireFound(sysDictItemService.getById(id)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String create(SysDictItemCreateParam param) {
        String actor = access.require("Admin:SysDictItem:Create");
        var row = sysDictItemConverter.create(param);
        var dict = dictionaries.lockById(param.sysDictId());
        if (Boolean.TRUE.equals(dict.getLocked())) throw AdminBusinessException.conflict("锁定字典不能手工新增项");
        if (row.getDisabled() == null) row.setDisabled(false);
        row.setCreateId(actor);
        checkWrite(sysDictItemService.save(row), "新增失败");
        return row.getSysDictItemId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String edit(SysDictItemUpdateParam param) {
        String actor = access.require("Admin:SysDictItem:Update");
        var dict = dictionaries.lockById(param.sysDictId());
        var row = sysDictItemService.lockById(param.sysDictItemId());
        checkVersion(row.getVersion(), param.version());
        if (!param.sysDictId().equals(row.getSysDictId())) throw AdminBusinessException.conflict("字典项不能跨字典移动");
        if (Boolean.TRUE.equals(dict.getLocked()) && !param.dictItemValue().equals(row.getDictItemValue()))
            throw AdminBusinessException.conflict("锁定字典项不能修改编码");
        sysDictItemConverter.edit(param, row);
        if (row.getDisabled() == null) row.setDisabled(false);
        row.setUpdateId(actor);
        checkWrite(sysDictItemService.updateById(row), "记录已变化，请刷新");
        return row.getSysDictItemId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(String id, long version) {
        String actor = access.require("Admin:SysDictItem:Delete");
        var dict = dictionaries.lockById(requireFound(sysDictItemService.getById(id)).getSysDictId());
        var row = sysDictItemService.lockById(id);
        checkVersion(row.getVersion(), version);
        if (Boolean.TRUE.equals(dict.getLocked())) throw AdminBusinessException.conflict("锁定字典项不能删除");
        sysDictItemService.delete(row, actor);
    }
}
