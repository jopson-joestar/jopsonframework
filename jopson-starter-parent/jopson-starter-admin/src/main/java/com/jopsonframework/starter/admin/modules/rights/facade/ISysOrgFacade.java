package com.jopsonframework.starter.admin.modules.rights.facade;

import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysOrgRow;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysOrgCreateParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysOrgUpdateParam;
import java.util.List;
import java.util.Map;

public interface ISysOrgFacade {
    SysOrgRow detail(String id);

    String create(SysOrgCreateParam param);

    String edit(SysOrgUpdateParam param);

    void delete(String id, long version);

    List<SysOrgRow> tree();

    Map<String, String> names(List<String> codes);

    String name(String sysOrgCode);
}
