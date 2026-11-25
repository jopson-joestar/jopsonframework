import type { LoginUser } from '#/api';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { LOGIN_PATH } from '@vben/constants';
import { resetAllStores, useAccessStore, useUserStore } from '@vben/stores';
import { defineStore } from 'pinia';

import { getUserInfoApi, loginApi, logoutApi, switchRightsApi } from '#/api';
import { refreshCsrf } from '#/api/request';

const channel = 'jorsy-admin-web:session';
const home = '/workspace';

function safeReturn(value: unknown) {
  return typeof value === 'string' && /^\/(?!\/)/.test(value) && !value.includes('\\') && !value.startsWith('/auth')
    ? value : home;
}

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter();
  const loginLoading = ref(false);
  const user = ref<LoginUser>();
  let checked = false;
  let restoring: Promise<void> | undefined;

  function apply(current: LoginUser) {
    user.value = current;
    useUserStore().setUserInfo({
      ...current,
      avatar: '',
      roles: current.identity ? [current.identity.sysRoleCode] : [],
    });
    useAccessStore().setAccessCodes(current.permissions);
    checked = true;
  }

  function restore() {
    if (checked) return Promise.resolve();
    restoring ??= getUserInfoApi().then(apply).catch((error) => {
      if (error?.code !== 401 && error?.code !== 404) throw error;
      checked = true;
    }).finally(() => { restoring = undefined; });
    return restoring;
  }

  function restart(path: string) {
    resetAllStores();
    window.history.replaceState(null, '', router.resolve(path).href);
    window.location.reload();
  }

  function notify() {
    localStorage.setItem(channel, crypto.randomUUID());
  }
  window.addEventListener('storage', (event) => {
    if (event.key === channel) restart(home);
  });

  function expired() {
    if (user.value) {
      const location = router.resolve({ path: LOGIN_PATH, query: { redirect: router.currentRoute.value.fullPath } });
      restart(location.fullPath);
    }
  }

  async function authLogin(params: Record<string, string>) {
    loginLoading.value = true;
    try {
      await loginApi(params.username ?? '', params.password ?? '');
      await refreshCsrf();
      resetAllStores();
      apply(await getUserInfoApi());
      notify();
      await router.replace(safeReturn(router.currentRoute.value.query.redirect));
    } finally {
      loginLoading.value = false;
    }
  }

  async function switchIdentity(id: string) {
    await switchRightsApi(id);
    notify();
    restart(home);
  }

  async function logout(redirect = true) {
    const previous = router.currentRoute.value.fullPath;
    await logoutApi();
    notify();
    restart(router.resolve({ path: LOGIN_PATH, query: redirect ? { redirect: previous } : {} }).fullPath);
  }

  function $reset() {
    user.value = undefined;
    checked = false;
    loginLoading.value = false;
  }

  return { $reset, user, authLogin, expired, loginLoading, logout, restore, switchIdentity };
});
