package com.jorsyframework.starter.admin.modules.basedata.facade;

import com.jorsyframework.starter.admin.modules.basedata.domain.dto.DictOption;
import com.jorsyframework.starter.admin.modules.basedata.domain.dto.DictSyncReport;
import com.jorsyframework.starter.admin.modules.basedata.domain.dto.SysDictRow;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.DictSyncRequest;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictCreateParam;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictQuery;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictUpdateParam;
import com.jorsyframework.starter.core.page.PageResult;
import java.util.List;

public interface ISysDictFacade {
    PageResult<SysDictRow> page(SysDictQuery param);

    SysDictRow detail(String id);

    String create(SysDictCreateParam param);

    String edit(SysDictUpdateParam param);

    void delete(String id, long version);

    List<DictOption> options(String sysDictCode);

    DictSyncReport syncByEnum(DictSyncRequest param);
}
