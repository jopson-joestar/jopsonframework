import { requestClient } from '#/api/request';

export interface LoginIdentity {
  sysRightsId: string;
  sysOrgId: string;
  sysOrgName: string;
  sysRoleId: string;
  sysRoleCode: string;
  sysRoleName: string;
  dataScope: string;
}

export interface LoginUser {
  userId: string;
  username: string;
  realName: string;
  identity: LoginIdentity;
  identities: LoginIdentity[];
  permissions: string[];
  administrator: boolean;
  extraOrgIds: string[];
}

export function getUserInfoApi() {
  return requestClient.get<LoginUser>('/admin/sys-login/current');
}

export function switchRightsApi(sysRightsId: string) {
  return requestClient.post<LoginUser>('/admin/sys-login/switch-rights', { sysRightsId });
}
