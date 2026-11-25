package com.jorsyframework.starter.admin.modules.rights.domain.dto;

import java.util.List;
import java.util.Set;

public record RoleResourceTree(List<SysResRow> rows, Set<String> checkedKeys, Set<String> assignableKeys, long version) {}
