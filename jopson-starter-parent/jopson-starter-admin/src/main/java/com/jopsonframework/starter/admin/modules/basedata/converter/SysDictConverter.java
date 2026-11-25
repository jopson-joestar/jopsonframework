package com.jopsonframework.starter.admin.modules.basedata.converter;

import com.jopsonframework.starter.admin.modules.basedata.domain.dto.SysDictRow;
import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysDict;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysDictCreateParam;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysDictUpdateParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysDictConverter {
    SysDictRow row(SysDict entity);

    SysDict create(SysDictCreateParam param);

    void edit(SysDictUpdateParam param, @MappingTarget SysDict entity);
}
