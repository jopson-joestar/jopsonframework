package com.jorsyframework.starter.admin.modules.basedata.domain.dto;

public record DictSyncReport(int scannedCount, int createdDictCount, int updatedDictCount, int createdItemCount,
                             int updatedItemCount, int deletedItemCount, int unchangedItemCount) {}
