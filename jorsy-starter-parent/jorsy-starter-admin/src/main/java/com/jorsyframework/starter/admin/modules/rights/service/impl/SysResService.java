package com.jorsyframework.starter.admin.modules.rights.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.jorsyframework.starter.admin.common.mybatis.AuditConditions;
import com.jorsyframework.starter.admin.modules.rights.domain.entity.SysRes;
import com.jorsyframework.starter.admin.modules.rights.mapper.SysResMapper;
import com.jorsyframework.starter.admin.modules.rights.service.ISysResService;
import java.util.List;
import org.springframework.stereotype.Service;

import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.checkWrite;
import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.requireFound;

@Service
public class SysResService extends ServiceImpl<SysResMapper, SysRes> implements ISysResService {
    @Override
    public List<SysRes> ordered() {
        return lambdaQuery().orderByAsc(SysRes::getSortNo, SysRes::getRouteName).list();
    }

    @Override
    public boolean hasChildren(String id) {
        return lambdaQuery().eq(SysRes::getParentId, id).exists();
    }

    @Override
    public SysRes lockById(String id) {
        return requireFound(lambdaQuery().eq(SysRes::getSysResId, id).last("FOR UPDATE").one());
    }

    @Override
    public void delete(SysRes row, String actor) {
        checkWrite(update(AuditConditions.logicalDelete(SysRes.class, row, SysRes::getSysResId, actor)), "删除失败，请刷新");
    }
}
