package com.jopsonframework.starter.admin.modules.basedata.converter;

import com.jopsonframework.starter.admin.modules.basedata.domain.dto.SysDictItemRow;
import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysDictItem;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysDictItemCreateParam;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysDictItemUpdateParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysDictItemConverter {
    SysDictItemRow row(SysDictItem entity);

    SysDictItem create(SysDictItemCreateParam param);

    void edit(SysDictItemUpdateParam param, @MappingTarget SysDictItem entity);
}
