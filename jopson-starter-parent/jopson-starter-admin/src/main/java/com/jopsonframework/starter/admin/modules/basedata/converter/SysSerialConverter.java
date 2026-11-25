package com.jopsonframework.starter.admin.modules.basedata.converter;

import com.jopsonframework.starter.admin.modules.basedata.domain.dto.SysSerialRow;
import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysSerial;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysSerialCreateParam;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysSerialUpdateParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysSerialConverter {
    SysSerialRow row(SysSerial entity);

    SysSerial create(SysSerialCreateParam param);

    void edit(SysSerialUpdateParam param, @MappingTarget SysSerial entity);
}
