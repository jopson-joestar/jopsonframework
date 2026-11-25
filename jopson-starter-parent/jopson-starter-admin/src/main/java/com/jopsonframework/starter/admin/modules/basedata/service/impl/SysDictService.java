package com.jopsonframework.starter.admin.modules.basedata.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.jopsonframework.starter.admin.common.mybatis.AuditConditions;
import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysDict;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysDictQuery;
import com.jopsonframework.starter.admin.modules.basedata.mapper.SysDictMapper;
import com.jopsonframework.starter.admin.modules.basedata.service.ISysDictService;
import com.jopsonframework.starter.core.mybatis.MybatisPages;
import com.jopsonframework.starter.core.page.PageQuery;
import com.jopsonframework.starter.core.page.PageResult;
import org.springframework.stereotype.Service;

import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.checkWrite;
import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.requireFound;

@Service
public class SysDictService extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {
    @Override
    public SysDict byCode(String code) {
        return lambdaQuery().eq(SysDict::getSysDictCode, code).one();
    }

    @Override
    public SysDict lockCode(String code) {
        return lambdaQuery().eq(SysDict::getSysDictCode, code).last("FOR UPDATE").one();
    }

    @Override
    public PageResult<SysDict> page(SysDictQuery param) {
        var filter = Wrappers.lambdaQuery(SysDict.class);
        filter.like(param.sysDictCode() != null && !param.sysDictCode().isBlank(), SysDict::getSysDictCode, param.sysDictCode());
        filter.like(param.sysDictName() != null && !param.sysDictName().isBlank(), SysDict::getSysDictName, param.sysDictName());
        filter.eq(param.locked() != null, SysDict::getLocked, param.locked());
        AuditConditions.createdBetween(filter, param.createTimeBegin(), param.createTimeEnd());
        filter.orderByDesc(SysDict::getCreateTime).orderByDesc(SysDict::getSysDictId);
        var pagination = MybatisPages.<SysDict>toPage(new PageQuery(param.currentPage(), param.pageSize()));
        return MybatisPages.toResult(page(pagination, filter));
    }

    @Override
    public SysDict lockById(String id) {
        return requireFound(lambdaQuery().eq(SysDict::getSysDictId, id).last("FOR UPDATE").one());
    }

    @Override
    public void delete(SysDict row, String actor) {
        checkWrite(update(AuditConditions.logicalDelete(SysDict.class, row, SysDict::getSysDictId, actor)), "删除失败，请刷新");
    }
}
