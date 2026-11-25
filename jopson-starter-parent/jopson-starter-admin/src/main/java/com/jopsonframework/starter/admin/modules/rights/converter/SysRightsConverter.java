package com.jopsonframework.starter.admin.modules.rights.converter;

import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysRightsRow;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRights;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRightsCreateParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRightsUpdateParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysRightsConverter {
    SysRightsRow row(SysRights entity);

    SysRights create(SysRightsCreateParam param);

    void edit(SysRightsUpdateParam param, @MappingTarget SysRights entity);
}
