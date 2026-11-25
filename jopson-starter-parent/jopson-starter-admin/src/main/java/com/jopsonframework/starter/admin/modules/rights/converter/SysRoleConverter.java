package com.jopsonframework.starter.admin.modules.rights.converter;

import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysRoleRow;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysRole;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRoleCreateParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRoleUpdateParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysRoleConverter {
    SysRoleRow row(SysRole entity);

    SysRole create(SysRoleCreateParam param);

    void edit(SysRoleUpdateParam param, @MappingTarget SysRole entity);
}
