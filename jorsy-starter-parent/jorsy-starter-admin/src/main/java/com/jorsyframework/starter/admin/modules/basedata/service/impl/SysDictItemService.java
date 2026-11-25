package com.jorsyframework.starter.admin.modules.basedata.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.jorsyframework.starter.admin.common.mybatis.AuditConditions;
import com.jorsyframework.starter.admin.modules.basedata.domain.entity.SysDictItem;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictItemQuery;
import com.jorsyframework.starter.admin.modules.basedata.mapper.SysDictItemMapper;
import com.jorsyframework.starter.admin.modules.basedata.service.ISysDictItemService;
import com.jorsyframework.starter.core.mybatis.MybatisPages;
import com.jorsyframework.starter.core.page.PageQuery;
import com.jorsyframework.starter.core.page.PageResult;
import java.util.List;
import org.springframework.stereotype.Service;

import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.checkWrite;
import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.requireFound;

@Service
public class SysDictItemService extends ServiceImpl<SysDictItemMapper, SysDictItem> implements ISysDictItemService {
    @Override
    public List<SysDictItem> forDict(String id) {
        return lambdaQuery().eq(SysDictItem::getSysDictId, id)
                .orderByAsc(SysDictItem::getSortNo, SysDictItem::getSysDictItemId).list();
    }

    @Override
    public PageResult<SysDictItem> page(SysDictItemQuery param) {
        var filter = Wrappers.lambdaQuery(SysDictItem.class);
        filter.eq(param.sysDictId() != null, SysDictItem::getSysDictId, param.sysDictId());
        filter.like(param.dictItemValue() != null && !param.dictItemValue().isBlank(), SysDictItem::getDictItemValue, param.dictItemValue());
        filter.like(param.dictItemLabel() != null && !param.dictItemLabel().isBlank(), SysDictItem::getDictItemLabel, param.dictItemLabel());
        AuditConditions.createdBetween(filter, param.createTimeBegin(), param.createTimeEnd());
        filter.orderByDesc(SysDictItem::getCreateTime).orderByDesc(SysDictItem::getSysDictItemId);
        var pagination = MybatisPages.<SysDictItem>toPage(new PageQuery(param.currentPage(), param.pageSize()));
        return MybatisPages.toResult(page(pagination, filter));
    }

    @Override
    public SysDictItem lockById(String id) {
        return requireFound(lambdaQuery().eq(SysDictItem::getSysDictItemId, id).last("FOR UPDATE").one());
    }

    @Override
    public void delete(SysDictItem row, String actor) {
        checkWrite(update(AuditConditions.logicalDelete(SysDictItem.class, row, SysDictItem::getSysDictItemId, actor)), "删除失败，请刷新");
    }
}
