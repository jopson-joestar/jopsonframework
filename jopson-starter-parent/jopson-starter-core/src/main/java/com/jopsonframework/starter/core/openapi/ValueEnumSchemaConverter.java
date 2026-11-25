package com.jopsonframework.starter.core.openapi;

import java.util.Iterator;
import java.util.LinkedHashSet;

import com.jopsonframework.starter.core.enums.ValueEnum;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.core.ResolvableType;
import tools.jackson.databind.JavaType;

/** swagger-core 已通过 @JsonValue 列出编码；接口的泛型 getter 还需要提供具体标量类型。 */
public final class ValueEnumSchemaConverter implements ModelConverter {
    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        Schema<?> schema = chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
        Class<?> raw = type.getType() instanceof JavaType javaType ? javaType.getRawClass()
                : ResolvableType.forType(type.getType()).resolve();
        if (schema == null || raw == null || !raw.isEnum() || !ValueEnum.class.isAssignableFrom(raw)) {
            return schema;
        }
        var target = schema;
        if (schema.get$ref() != null) {
            String ref = schema.get$ref();
            target = context.getDefinedModels().get(ref.substring(ref.lastIndexOf('/') + 1));
        }
        if (target != null) {
            Class<?> codeType = ResolvableType.forClass(raw).as(ValueEnum.class).getGeneric().resolve();
            String scalar = codeType == Integer.class ? "integer" : "string";
            var types = new LinkedHashSet<String>();
            types.add(scalar);
            if (target.getTypes() != null && target.getTypes().contains("null")) {
                types.add("null");
            }
            target.setType(scalar);
            target.setTypes(types);
        }
        return schema;
    }
}
