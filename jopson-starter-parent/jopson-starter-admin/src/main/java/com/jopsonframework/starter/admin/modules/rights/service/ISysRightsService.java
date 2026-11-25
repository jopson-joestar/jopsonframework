package com.jopsonframework.starter.admin.modules.rights.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRights;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRightsQuery;
import com.jopsonframework.starter.core.page.PageResult;

import java.util.List;

public interface ISysRightsService extends IService<SysRights> {
    List<SysRights> enabledForUser(String userId);

    boolean usesOrg(String id);

    boolean usesRole(String id);

    PageResult<SysRights> page(SysRightsQuery param);

    SysRights lockById(String id);

    void delete(SysRights row, String actor);
}
