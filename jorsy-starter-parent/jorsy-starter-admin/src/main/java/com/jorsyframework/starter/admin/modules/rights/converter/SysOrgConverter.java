package com.jorsyframework.starter.admin.modules.rights.converter;

import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysOrgRow;
import com.jorsyframework.starter.admin.modules.rights.domain.entity.SysOrg;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysOrgCreateParam;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysOrgUpdateParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysOrgConverter {
    SysOrgRow row(SysOrg entity);

    SysOrg create(SysOrgCreateParam param);

    void edit(SysOrgUpdateParam param, @MappingTarget SysOrg entity);
}
