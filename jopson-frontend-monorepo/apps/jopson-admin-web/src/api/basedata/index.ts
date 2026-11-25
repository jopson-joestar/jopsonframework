import type { SyncOperation, VersionedRow } from '#/api/admin-records';

import { postAdmin, recordsApi } from '#/api/admin-records';

export interface ConfigRow extends VersionedRow {
  sysConfigId: string;
  sysConfigCode: string;
  sysConfigName: string;
  sysConfigValue: null | string;
  remark: null | string;
  updateTime: null | string;
}

export interface SerialRow extends VersionedRow {
  sysSerialId: string;
  serialCode: string;
  serialName: string;
  serialPrefix: null | string;
  serialSuffix: null | string;
  serialDigit: number;
  dateFormat: null | string;
  sysData: boolean;
  updateTime: null | string;
}

export interface DictRow extends VersionedRow {
  sysDictId: string;
  sysDictCode: string;
  sysDictName: string;
  locked: boolean;
  remark: null | string;
}

export interface DictItemRow extends VersionedRow {
  sysDictItemId: string;
  sysDictId: string;
  dictItemValue: string;
  dictItemLabel: string;
  dictItemExtra: unknown;
  sortNo: null | number;
  labelColor: null | string;
  labelClass: null | string;
  disabled: boolean;
  remark: null | string;
}

interface DictSyncResult {
  scannedCount: number;
  createdDictCount: number;
  updatedDictCount: number;
  createdItemCount: number;
  updatedItemCount: number;
  deletedItemCount: number;
  unchangedItemCount: number;
}

export const configApi = recordsApi<ConfigRow>('sys-config', 'sysConfigId');
export const serialApi = recordsApi<SerialRow>('sys-serial', 'sysSerialId');
export const dictApi = recordsApi<DictRow>('sys-dict', 'sysDictId');
export const dictItemApi = recordsApi<DictItemRow>(
  'sys-dict-item',
  'sysDictItemId',
);

export function syncDictionary(operations: SyncOperation[]) {
  return postAdmin<DictSyncResult>('sys-dict', 'sync-by-enum', { operations });
}
