package com.jopsonframework.starter.admin.modules.rights.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRes;
import java.util.List;

public interface ISysResService extends IService<SysRes> {
    List<SysRes> ordered();

    boolean hasChildren(String id);

    SysRes lockById(String id);

    void delete(SysRes row, String actor);
}
