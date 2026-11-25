package org.example.jorsy;

import java.util.List;

import com.jorsyframework.starter.core.convert.ConvertContext;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
interface DocumentRowMapper {
    List<DocumentEndpoints.DocumentRow> toRows(List<DocumentEndpoints.Document> rows,
                                               @Context ConvertContext<String, String> names);

    @Mapping(target = "reviewerName", source = "reviewerId", qualifiedByName = "reviewerName")
    DocumentEndpoints.DocumentRow toRow(DocumentEndpoints.Document row, @Context ConvertContext<String, String> names);

    @Named("reviewerName")
    default String reviewerName(String id, @Context ConvertContext<String, String> names) {
        return names.get(id);
    }
}
