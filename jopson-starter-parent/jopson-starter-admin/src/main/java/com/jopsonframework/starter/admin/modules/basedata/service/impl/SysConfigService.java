package com.jopsonframework.starter.admin.modules.basedata.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.jopsonframework.starter.admin.common.mybatis.AuditConditions;
import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysConfig;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysConfigQuery;
import com.jopsonframework.starter.admin.modules.basedata.mapper.SysConfigMapper;
import com.jopsonframework.starter.admin.modules.basedata.service.ISysConfigService;
import com.jopsonframework.starter.core.mybatis.MybatisPages;
import com.jopsonframework.starter.core.page.PageQuery;
import com.jopsonframework.starter.core.page.PageResult;
import org.springframework.stereotype.Service;

import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.checkWrite;
import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.requireFound;

@Service
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {
    @Override
    public SysConfig byCode(String code) {
        return lambdaQuery().eq(SysConfig::getSysConfigCode, code).one();
    }

    @Override
    public PageResult<SysConfig> page(SysConfigQuery param) {
        var filter = Wrappers.lambdaQuery(SysConfig.class);
        filter.like(param.sysConfigCode() != null && !param.sysConfigCode().isBlank(), SysConfig::getSysConfigCode, param.sysConfigCode());
        filter.like(param.sysConfigName() != null && !param.sysConfigName().isBlank(), SysConfig::getSysConfigName, param.sysConfigName());
        AuditConditions.createdBetween(filter, param.createTimeBegin(), param.createTimeEnd());
        filter.orderByDesc(SysConfig::getCreateTime).orderByDesc(SysConfig::getSysConfigId);
        var pagination = MybatisPages.<SysConfig>toPage(new PageQuery(param.currentPage(), param.pageSize()));
        return MybatisPages.toResult(page(pagination, filter));
    }

    @Override
    public SysConfig lockById(String id) {
        return requireFound(lambdaQuery().eq(SysConfig::getSysConfigId, id).last("FOR UPDATE").one());
    }

    @Override
    public void delete(SysConfig row, String actor) {
        checkWrite(update(AuditConditions.logicalDelete(SysConfig.class, row, SysConfig::getSysConfigId, actor)), "删除失败，请刷新");
    }
}
