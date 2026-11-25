package com.jorsyframework.starter.admin.modules.rights.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.jorsyframework.starter.admin.modules.rights.domain.entity.SysOrg;
import com.jorsyframework.starter.admin.modules.rights.domain.entity.SysRightsExtra;
import java.util.Collection;
import java.util.List;

public interface ISysRightsExtraService extends IService<SysRightsExtra> {
    List<SysRightsExtra> forRights(Collection<String> ids);

    boolean usesOrg(String id);

    void replace(String rightsId, List<SysOrg> selected, String actor);

    void delete(SysRightsExtra row, String actor);
}
