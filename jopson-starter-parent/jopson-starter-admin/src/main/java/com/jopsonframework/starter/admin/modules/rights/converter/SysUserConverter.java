package com.jopsonframework.starter.admin.modules.rights.converter;

import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysUserRow;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysUser;
import com.jopsonframework.starter.core.convert.ConvertContext;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SysUserConverter {
    @Mapping(target = "createName", source = "createId", qualifiedByName = "auditName")
    @Mapping(target = "updateName", source = "updateId", qualifiedByName = "auditName")
    SysUserRow toRow(SysUser user, @Context ConvertContext<String, String> names);

    @Named("auditName")
    default String auditName(String id, @Context ConvertContext<String, String> names) {
        return names.get(id);
    }
}
