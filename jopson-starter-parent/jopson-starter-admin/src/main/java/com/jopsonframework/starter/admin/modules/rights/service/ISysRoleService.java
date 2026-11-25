package com.jopsonframework.starter.admin.modules.rights.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRole;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRoleQuery;
import com.jopsonframework.starter.core.page.PageResult;
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
