package com.jopsonframework.starter.admin.modules.rights.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysOrg;
import java.util.Collection;
import java.util.List;

public interface ISysOrgService extends IService<SysOrg> {
    List<SysOrg> ordered();

    List<SysOrg> byCodes(Collection<String> codes);

    String nextCode(String parentId);

    boolean hasChildren(String id);

    SysOrg lockById(String id);

    void delete(SysOrg row, String actor);
}
