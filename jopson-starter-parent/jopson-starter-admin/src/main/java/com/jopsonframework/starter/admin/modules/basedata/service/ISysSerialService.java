package com.jopsonframework.starter.admin.modules.basedata.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysSerial;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysSerialQuery;
import com.jopsonframework.starter.core.page.PageResult;

public interface ISysSerialService extends IService<SysSerial> {
    SysSerial lockCode(String code);

    String allocate(String code, String actor);

    PageResult<SysSerial> page(SysSerialQuery param);

    SysSerial lockById(String id);

    void delete(SysSerial row, String actor);
}
