package com.jopsonframework.starter.admin.modules.basedata.converter;

import com.jopsonframework.starter.admin.modules.basedata.domain.dto.SysConfigRow;
import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysConfig;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysConfigCreateParam;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysConfigUpdateParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysConfigConverter {
    SysConfigRow row(SysConfig entity);

    SysConfig create(SysConfigCreateParam param);

    void edit(SysConfigUpdateParam param, @MappingTarget SysConfig entity);
}
