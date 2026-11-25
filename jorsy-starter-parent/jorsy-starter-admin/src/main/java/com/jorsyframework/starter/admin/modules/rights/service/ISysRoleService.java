package com.jorsyframework.starter.admin.modules.rights.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.jorsyframework.starter.admin.modules.rights.domain.entity.SysRole;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysRoleQuery;
import com.jorsyframework.starter.core.page.PageResult;
import java.util.List;
import java.util.Set;

public interface ISysRoleService extends IService<SysRole> {
    void lockHierarchy();

    Set<String> descendants(String id);

    PageResult<SysRole> page(SysRoleQuery param);

    SysRole lockById(String id);

    void delete(SysRole row, String actor);

    List<SysRole> ordered();
}
