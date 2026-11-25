package com.jorsyframework.starter.admin.modules.rights.domain.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RoleResources(@NotBlank @Size(min = 32, max = 32) String sysRoleId,
                            @NotNull @PositiveOrZero Long version,
                            @NotNull List<@NotBlank @Size(min = 32, max = 32) String> sysResIds) {}
