package com.jopsonframework.starter.admin.modules.rights.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysUser;
import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysUserStatus;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysUserPageQuery;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysUserUpdateParam;
import com.jopsonframework.starter.core.page.PageResult;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ISysUserService extends IService<SysUser> {
    PageResult<SysUser> page(SysUserPageQuery query);
    Optional<SysUser> find(String id);
    Optional<SysUser> loginCandidate(String code);
    Map<String, String> names(Set<String> ids);
    void insert(SysUser user);
    int edit(SysUserUpdateParam form, String operatorId);
    int changeStatus(String id, long version, SysUserStatus from, SysUserStatus to, String operatorId);
    int delete(String id, long version, SysUserStatus from, String operatorId);
}
