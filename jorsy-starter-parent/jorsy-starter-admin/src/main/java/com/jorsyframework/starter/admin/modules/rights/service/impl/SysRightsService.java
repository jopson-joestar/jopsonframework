package com.jorsyframework.starter.admin.modules.rights.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.jorsyframework.starter.admin.common.mybatis.AuditConditions;
import com.jorsyframework.starter.admin.modules.rights.domain.entity.SysRights;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysRightsQuery;
import com.jorsyframework.starter.admin.modules.rights.mapper.SysRightsMapper;
import com.jorsyframework.starter.admin.modules.rights.service.ISysRightsService;
import com.jorsyframework.starter.core.mybatis.MybatisPages;
import com.jorsyframework.starter.core.page.PageQuery;
import com.jorsyframework.starter.core.page.PageResult;
import org.springframework.stereotype.Service;
import java.util.List;

import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.checkWrite;
import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.requireFound;

@Service
public class SysRightsService extends ServiceImpl<SysRightsMapper, SysRights> implements ISysRightsService {
    @Override
    public List<SysRights> enabledForUser(String userId) {
        return lambdaQuery().eq(SysRights::getSysUserId, userId).eq(SysRights::getEnabled, true)
                .orderByDesc(SysRights::getDefaulted).orderByAsc(SysRights::getSysRightsId).list();
    }

    @Override
    public boolean usesOrg(String id) {
        return lambdaQuery().eq(SysRights::getSysOrgId, id).exists();
    }

    @Override
    public boolean usesRole(String id) {
        return lambdaQuery().eq(SysRights::getSysRoleId, id).exists();
    }

    @Override
    public PageResult<SysRights> page(SysRightsQuery param) {
        var filter = Wrappers.lambdaQuery(SysRights.class);
        filter.eq(param.sysUserId() != null, SysRights::getSysUserId, param.sysUserId());
        filter.eq(param.sysOrgId() != null, SysRights::getSysOrgId, param.sysOrgId());
        filter.eq(param.sysRoleId() != null, SysRights::getSysRoleId, param.sysRoleId());
        filter.eq(param.enabled() != null, SysRights::getEnabled, param.enabled());
        filter.eq(param.defaulted() != null, SysRights::getDefaulted, param.defaulted());
        AuditConditions.createdBetween(filter, param.createTimeBegin(), param.createTimeEnd());
        filter.orderByDesc(SysRights::getCreateTime).orderByDesc(SysRights::getSysRightsId);
        var pagination = MybatisPages.<SysRights>toPage(new PageQuery(param.currentPage(), param.pageSize()));
        return MybatisPages.toResult(page(pagination, filter));
    }

    @Override
    public SysRights lockById(String id) {
        return requireFound(lambdaQuery().eq(SysRights::getSysRightsId, id).last("FOR UPDATE").one());
    }

    @Override
    public void delete(SysRights row, String actor) {
        checkWrite(update(AuditConditions.logicalDelete(SysRights.class, row, SysRights::getSysRightsId, actor)), "删除失败，请刷新");
    }
}
