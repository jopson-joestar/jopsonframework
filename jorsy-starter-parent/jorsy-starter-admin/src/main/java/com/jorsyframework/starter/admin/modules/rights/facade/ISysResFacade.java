package com.jorsyframework.starter.admin.modules.rights.facade;

import com.jorsyframework.starter.admin.modules.rights.domain.dto.ResourceImportReport;
import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysResRow;
import com.jorsyframework.starter.admin.modules.rights.domain.param.ResourceImport;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysResCreateParam;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysResUpdateParam;
import java.util.List;

public interface ISysResFacade {
    SysResRow detail(String id);

    String create(SysResCreateParam param);

    String edit(SysResUpdateParam param);

    void delete(String id, long version);

    List<SysResRow> tree();

    ResourceImportReport importJson(ResourceImport param);
}
