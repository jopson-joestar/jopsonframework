package com.jorsyframework.starter.admin.modules.rights.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.jorsyframework.starter.admin.modules.rights.domain.entity.SysRoleAuth;
import java.util.Collection;
import java.util.Set;

public interface ISysRoleAuthService extends IService<SysRoleAuth> {
    Set<String> granted(String roleId);

    boolean hasAny(Collection<String> roles, Collection<String> resources);

    void replace(String roleId, Set<String> requested, Set<String> existing);

    void removeResource(String id);
}
