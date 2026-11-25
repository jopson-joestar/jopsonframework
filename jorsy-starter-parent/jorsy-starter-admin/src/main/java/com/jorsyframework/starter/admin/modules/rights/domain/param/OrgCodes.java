package com.jorsyframework.starter.admin.modules.rights.domain.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrgCodes(@NotNull List<@NotBlank String> codes) {}
