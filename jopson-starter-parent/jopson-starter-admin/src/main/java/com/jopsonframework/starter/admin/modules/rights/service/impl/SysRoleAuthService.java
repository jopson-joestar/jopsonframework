package com.jopsonframework.starter.admin.modules.rights.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRoleAuth;
import com.jopsonframework.starter.admin.modules.rights.mapper.SysRoleAuthMapper;
import com.jopsonframework.starter.admin.modules.rights.service.ISysRoleAuthService;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SysRoleAuthService extends ServiceImpl<SysRoleAuthMapper, SysRoleAuth> implements ISysRoleAuthService {
    @Override
    public Set<String> granted(String roleId) {
        return lambdaQuery().eq(SysRoleAuth::getSysRoleId, roleId).list().stream()
                .map(SysRoleAuth::getSysResId).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public boolean hasAny(Collection<String> roles, Collection<String> resources) {
        return !roles.isEmpty() && !resources.isEmpty() && lambdaQuery()
                .in(SysRoleAuth::getSysRoleId, roles).in(SysRoleAuth::getSysResId, resources).exists();
    }

    @Override
    public void replace(String roleId, Set<String> requested, Set<String> existing) {
        Set<String> removed = new HashSet<>(existing);
        removed.removeAll(requested);
        if (!removed.isEmpty()) {
            lambdaUpdate().eq(SysRoleAuth::getSysRoleId, roleId).in(SysRoleAuth::getSysResId, removed).remove();
        }
        for (String id : requested) {
            if (existing.contains(id)) continue;
            var grant = new SysRoleAuth();
            grant.setSysRoleId(roleId);
            grant.setSysResId(id);
            save(grant);
        }
    }

    @Override
    public void removeResource(String id) {
        lambdaUpdate().eq(SysRoleAuth::getSysResId, id).remove();
    }
}
