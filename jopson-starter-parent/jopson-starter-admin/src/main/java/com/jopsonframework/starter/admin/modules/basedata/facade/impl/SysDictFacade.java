package com.jopsonframework.starter.admin.modules.basedata.facade.impl;

import com.jopsonframework.starter.admin.common.exception.AdminBusinessException;
import com.jopsonframework.starter.admin.modules.basedata.converter.SysDictConverter;
import com.jopsonframework.starter.admin.modules.basedata.domain.dto.DictOption;
import com.jopsonframework.starter.admin.modules.basedata.domain.dto.DictSyncReport;
import com.jopsonframework.starter.admin.modules.basedata.domain.dto.SysDictRow;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.DictSyncRequest;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysDictCreateParam;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysDictQuery;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysDictUpdateParam;
import com.jopsonframework.starter.admin.modules.basedata.facade.ISysDictFacade;
import com.jopsonframework.starter.admin.modules.basedata.service.ISysDictItemService;
import com.jopsonframework.starter.admin.modules.basedata.service.ISysDictService;
import com.jopsonframework.starter.admin.modules.rights.access.SysUserOperatorProvider;
import com.jopsonframework.starter.core.page.PageResult;
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
public class SysDictFacade implements ISysDictFacade {
    private final SysUserOperatorProvider access;
    private final ISysDictService sysDictService;
    private final SysDictConverter sysDictConverter;
    private final ISysDictItemService items;
    private final DictionarySync sync;

    @Override
    public PageResult<SysDictRow> page(SysDictQuery param) {
        access.require("Admin:SysDict:View");
        var page = sysDictService.page(param);
        return new PageResult<>(page.items().stream().map(sysDictConverter::row).toList(), page.total(), page.currentPage(), page.pageSize());
    }

    @Override
    public SysDictRow detail(String id) {
        access.require("Admin:SysDict:View");
        return sysDictConverter.row(requireFound(sysDictService.getById(id)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String create(SysDictCreateParam param) {
        String actor = access.require("Admin:SysDict:Create");
        var row = sysDictConverter.create(param);
        row.setLocked(false);
        row.setCreateId(actor);
        checkWrite(sysDictService.save(row), "新增失败");
        return row.getSysDictId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String edit(SysDictUpdateParam param) {
        String actor = access.require("Admin:SysDict:Update");
        var row = sysDictService.lockById(param.sysDictId());
        checkVersion(row.getVersion(), param.version());
        if (Boolean.TRUE.equals(row.getLocked()) && !row.getSysDictCode().equals(param.sysDictCode()))
            throw AdminBusinessException.conflict("锁定字典不能修改编码");
        sysDictConverter.edit(param, row);
        row.setUpdateId(actor);
        checkWrite(sysDictService.updateById(row), "记录已变化，请刷新");
        return row.getSysDictId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(String id, long version) {
        String actor = access.require("Admin:SysDict:Delete");
        var row = sysDictService.lockById(id);
        checkVersion(row.getVersion(), version);
        if (Boolean.TRUE.equals(row.getLocked())) throw AdminBusinessException.conflict("锁定字典不能删除");
        for (var item : items.forDict(id)) items.delete(item, actor);
        sysDictService.delete(row, actor);
    }

    @Override
    public List<DictOption> options(String sysDictCode) {
        access.require("Admin:SysDict:View");
        var dict = sysDictService.byCode(sysDictCode);
        if (dict == null) throw AdminBusinessException.missing("字典不存在");
        return items.forDict(dict.getSysDictId()).stream().map(DictOption::from).toList();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DictSyncReport syncByEnum(DictSyncRequest param) {
        String actor = access.require("Admin:SysDict:Sync");
        return sync.run(param.operations(), actor);
    }
}
