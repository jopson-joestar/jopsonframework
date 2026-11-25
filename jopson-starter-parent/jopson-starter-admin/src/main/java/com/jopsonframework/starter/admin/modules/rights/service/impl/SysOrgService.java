package com.jopsonframework.starter.admin.modules.rights.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.jopsonframework.starter.admin.common.exception.AdminBusinessException;
import com.jopsonframework.starter.admin.common.mybatis.AuditConditions;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysOrg;
import com.jopsonframework.starter.admin.modules.rights.mapper.SysOrgMapper;
import com.jopsonframework.starter.admin.modules.rights.service.ISysOrgService;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.checkWrite;
import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.requireFound;

@Service
public class SysOrgService extends ServiceImpl<SysOrgMapper, SysOrg> implements ISysOrgService {
    @Override
    public List<SysOrg> ordered() {
        return lambdaQuery().orderByAsc(SysOrg::getSortNo, SysOrg::getSysOrgCode).list();
    }

    @Override
    public List<SysOrg> byCodes(Collection<String> codes) {
        return codes.isEmpty() ? List.of() : list(Wrappers.lambdaQuery(SysOrg.class).in(SysOrg::getSysOrgCode, codes));
    }

    @Override
    public String nextCode(String parentId) {
        if (parentId == null) return "0001";
        String prefix = lockById(parentId).getSysOrgCode();
        var siblings = list(Wrappers.lambdaQuery(SysOrg.class).eq(SysOrg::getParentId, parentId)
                .orderByDesc(SysOrg::getSysOrgCode).last("LIMIT 1 FOR UPDATE"));
        int next = siblings.isEmpty() ? 1 : Integer.parseInt(siblings.getFirst().getSysOrgCode().substring(prefix.length())) + 1;
        if (next > 9999 || prefix.length() + 4 > 50) throw AdminBusinessException.conflict("组织编码已达上限");
        return prefix + String.format(java.util.Locale.ROOT, "%04d", next);
    }

    @Override
    public boolean hasChildren(String id) {
        return lambdaQuery().eq(SysOrg::getParentId, id).exists();
    }

    @Override
    public SysOrg lockById(String id) {
        return requireFound(lambdaQuery().eq(SysOrg::getSysOrgId, id).last("FOR UPDATE").one());
    }

    @Override
    public void delete(SysOrg row, String actor) {
        checkWrite(update(AuditConditions.logicalDelete(SysOrg.class, row, SysOrg::getSysOrgId, actor)), "删除失败，请刷新");
    }
}
