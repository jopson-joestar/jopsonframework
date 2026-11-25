package com.jopsonframework.starter.admin.modules.basedata.domain.param;

import com.jopsonframework.starter.admin.common.enums.SyncOperation;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record DictSyncRequest(@NotEmpty Set<@NotNull SyncOperation> operations) {}
