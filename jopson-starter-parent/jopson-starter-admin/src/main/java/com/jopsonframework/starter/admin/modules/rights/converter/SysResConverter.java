package com.jopsonframework.starter.admin.modules.rights.converter;

import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysResRow;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRes;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysResCreateParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysResUpdateParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysResConverter {
    SysResRow row(SysRes entity);

    SysRes create(SysResCreateParam param);

    void edit(SysResUpdateParam param, @MappingTarget SysRes entity);
}
