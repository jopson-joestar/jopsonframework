package com.jopsonframework.starter.admin.modules.rights.domain.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record RightsEnabled(@NotBlank @Size(min = 32, max = 32) String sysRightsId,
                            @NotNull @PositiveOrZero Long version, @NotNull Boolean enabled) {}
