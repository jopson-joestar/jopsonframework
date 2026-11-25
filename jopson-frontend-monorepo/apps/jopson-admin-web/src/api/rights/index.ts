import type { SyncOperation, VersionedRow } from '#/api/admin-records';

import { postAdmin, recordsApi } from '#/api/admin-records';

export type OrgType = 'C' | 'D' | 'G';
export type DataScope = '1' | '2' | '3' | '9';
export type UserStatus = -100 | 0 | 1 | 10;
export type ResourceType = 'BUTTON' | 'FOLDER' | 'IFRAME' | 'LINK' | 'MENU';

export interface OrgRow extends VersionedRow {
  sysOrgId: string;
  parentId: null | string;
  sysOrgCode: string;
  sysOrgName: string;
  sysOrgType: OrgType;
  sortNo: null | number;
  sysData: boolean;
  children: OrgRow[];
}

export interface UserRow extends VersionedRow {
  sysUserId: string;
  sysUserCode: string;
  sysUserName: string;
  sysUserMobile: null | string;
  sysUserEmail: null | string;
  sysUserQq: null | string;
  sysUserStatus: UserStatus;
  sysData: boolean;
  createTime: null | string;
}

export interface RoleRow extends VersionedRow {
  sysRoleId: string;
  parentId: null | string;
  sysRoleCode: string;
  sysRoleName: string;
  remark: null | string;
  sysData: boolean;
  children: RoleRow[];
}

export interface ResourceValues {
  sortNo?: null | number;
  sysResType: ResourceType;
  permission?: null | string;
  routeName: string;
  routePath?: null | string;
  component?: null | string;
  redirect?: null | string;
  routeMeta: Record<string, unknown>;
  remark?: null | string;
}

export interface ResourceRow extends ResourceValues, VersionedRow {
  sysResId: string;
  parentId: null | string;
  children: ResourceRow[];
}

export interface ResourceImportNode extends ResourceValues {
  children?: ResourceImportNode[];
}

export interface RightsRow extends VersionedRow {
  sysRightsId: string;
  sysUserId: string;
  sysUserName: null | string;
  sysUserCode: string;
  sysOrgId: string;
  sysOrgName: null | string;
  sysOrgCode: string;
  sysRoleId: string;
  sysRoleName: null | string;
  sysRoleCode: string;
  dataScope: DataScope;
  enabled: boolean;
  defaulted: boolean;
  sysData: boolean;
  extraOrgIds: string[];
  extraOrgNames: string[];
}

export interface RoleResourceTree {
  rows: ResourceRow[];
  checkedKeys: string[];
  assignableKeys: string[];
  version: number;
}

export const orgApi = recordsApi<OrgRow>('sys-org', 'sysOrgId');
export const userApi = recordsApi<UserRow>('sys-user', 'sysUserId');
export const roleApi = recordsApi<RoleRow>('sys-role', 'sysRoleId');
export const resourceApi = recordsApi<ResourceRow>('sys-res', 'sysResId');
export const rightsApi = recordsApi<RightsRow>('sys-rights', 'sysRightsId');

export function changeUserStatus(row: UserRow, sysStatus: UserStatus) {
  return postAdmin<void>('sys-user', 'update-status', undefined, {
    sysUserId: row.sysUserId,
    sysStatus,
    version: row.version,
  });
}

export function setRightsEnabled(row: RightsRow, enabled: boolean) {
  return postAdmin<void>('sys-rights', 'update-enabled', {
    sysRightsId: row.sysRightsId,
    version: row.version,
    enabled,
  });
}

export function getRoleResources(sysRoleId: string) {
  return postAdmin<RoleResourceTree>(
    'sys-role-auth',
    'list-auth-res',
    undefined,
    { sysRoleId },
  );
}

export function saveRoleResources(
  sysRoleId: string,
  version: number,
  sysResIds: string[],
) {
  return postAdmin<void>('sys-role-auth', 'auth-res', {
    sysRoleId,
    version,
    sysResIds,
  });
}

export function importResources(
  mode: 'FULL' | 'INCREMENTAL',
  operations: SyncOperation[],
  resources: ResourceImportNode[],
) {
  return postAdmin<{
    addedCount: number;
    updatedCount: number;
    deletedCount: number;
  }>('sys-res', 'import-json', { mode, operations, resources });
}
