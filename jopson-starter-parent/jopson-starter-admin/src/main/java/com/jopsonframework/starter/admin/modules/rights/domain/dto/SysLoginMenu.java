package com.jopsonframework.starter.admin.modules.rights.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import java.util.List;

@Getter
@Setter
public class SysLoginMenu {
    @JsonIgnore private String id;
    @JsonIgnore private String parentId;
    private String name;
    private String path;
    private String component;
    private String redirect;
    private JsonNode meta;
    private List<SysLoginMenu> children;
}
