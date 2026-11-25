package com.jopsonframework.starter.core.enums;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueEnumCodesTest {
    enum Duplicate implements ValueEnum<String> {
        FIRST, SECOND;

        public String getValue() { return "duplicate"; }
    }

    @Test
    void rejectsAmbiguousDefinitionsOnLookupAndSerialization() {
        assertThatThrownBy(() -> ValueEnumCodes.find(Duplicate.class, "duplicate"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("重复编码");
        assertThatThrownBy(() -> JsonMapper.builder().build().writeValueAsString(Duplicate.FIRST))
                .hasRootCauseInstanceOf(IllegalStateException.class);
    }
}
