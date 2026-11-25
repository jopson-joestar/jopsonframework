package com.jopsonframework.website.admin.mapping;

import lombok.Value;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ItemTestMapper {

    ItemDto toDto(Item source);

    @Value
    class Item {
        String name;
        int quantity;
    }

    record ItemDto(String name, int quantity) {}
}
