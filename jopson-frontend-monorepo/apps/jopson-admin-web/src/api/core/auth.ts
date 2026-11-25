import { refreshCsrf, requestClient } from '#/api/request';

export function loginApi(username: string, password: string) {
  return requestClient.post<void>('/auth/login', new URLSearchParams({ username, password }), {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  });
}

export async function logoutApi() {
  await requestClient.post<void>('/auth/logout');
  await refreshCsrf().catch(() => undefined);
}
