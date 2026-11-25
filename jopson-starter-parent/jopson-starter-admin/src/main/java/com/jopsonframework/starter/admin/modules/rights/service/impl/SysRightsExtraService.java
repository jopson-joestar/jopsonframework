package com.jopsonframework.starter.admin.modules.rights.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.jopsonframework.starter.admin.common.mybatis.AuditConditions;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysOrg;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRightsExtra;
import com.jopsonframework.starter.admin.modules.rights.mapper.SysRightsExtraMapper;
import com.jopsonframework.starter.admin.modules.rights.service.ISysRightsExtraService;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.checkWrite;

@Service
public class SysRightsExtraService extends ServiceImpl<SysRightsExtraMapper, SysRightsExtra> implements ISysRightsExtraService {
    @Override
    public List<SysRightsExtra> forRights(Collection<String> ids) {
        return ids.isEmpty() ? List.of() : list(Wrappers.lambdaQuery(SysRightsExtra.class).in(SysRightsExtra::getSysRightsId, ids)
                .orderByAsc(SysRightsExtra::getSysOrgCode));
    }

    @Override
    public boolean usesOrg(String id) {
        return lambdaQuery().eq(SysRightsExtra::getSysOrgId, id).exists();
    }

    @Override
    public void replace(String rightsId, List<SysOrg> selected, String actor) {
        var existing = forRights(List.of(rightsId));
        Set<String> requested = selected.stream().map(SysOrg::getSysOrgId).collect(Collectors.toSet());
        Set<String> retained = new HashSet<>();
        for (var row : existing) {
            if (requested.contains(row.getSysOrgId())) retained.add(row.getSysOrgId());
            else delete(row, actor);
        }
        for (var org : selected) {
            if (retained.contains(org.getSysOrgId())) continue;
            var row = new SysRightsExtra();
            row.setSysRightsId(rightsId);
            row.setSysOrgId(org.getSysOrgId());
            row.setSysOrgCode(org.getSysOrgCode());
            row.setCreateId(actor);
            checkWrite(save(row), "新增失败");
        }
    }

    @Override
    public void delete(SysRightsExtra row, String actor) {
        checkWrite(update(AuditConditions.logicalDelete(SysRightsExtra.class, row, SysRightsExtra::getSysRightsExtraId, actor)), "删除失败，请刷新");
    }
}
