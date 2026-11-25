import type { Router } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import { useAccessStore } from '@vben/stores';
import { startProgress, stopProgress } from '@vben/utils';

import { accessRoutes, coreRouteNames } from '#/router/routes';
import { useAuthStore } from '#/store';

import { generateAccess } from './access';

/**
 * 通用守卫配置
 * @param router
 */
function setupCommonGuard(router: Router) {
  // 记录已经加载的页面
  const loadedPaths = new Set<string>();

  router.beforeEach((to) => {
    to.meta.loaded = loadedPaths.has(to.path);

    // 页面加载进度条
    if (!to.meta.loaded && preferences.transition.progress) {
      startProgress();
    }
    return true;
  });

  router.afterEach((to) => {
    // 记录页面是否加载,如果已经加载，后续的页面切换动画等效果不在重复执行

    loadedPaths.add(to.path);

    // 关闭页面加载进度条
    if (preferences.transition.progress) {
      stopProgress();
    }
  });
}

/**
 * 权限访问守卫配置
 * @param router
 */
function setupAccessGuard(router: Router) {
  router.beforeEach(async (to) => {
    const auth = useAuthStore();
    if (to.path === LOGIN_PATH) {
      await auth.restore().catch(() => undefined);
      return auth.user ? '/workspace' : true;
    }
    if (coreRouteNames.includes(to.name as string)) return true;
    await auth.restore().catch(() => undefined);
    if (!auth.user) return { path: LOGIN_PATH, query: { redirect: to.fullPath }, replace: true };

    const access = useAccessStore();
    if (access.isAccessChecked) return true;
    const { accessibleMenus, accessibleRoutes } = await generateAccess({
      roles: [auth.user.identity.sysRoleCode], router, routes: accessRoutes,
    });
    access.setAccessMenus(accessibleMenus);
    access.setAccessRoutes(accessibleRoutes);
    access.setIsAccessChecked(true);
    return { ...router.resolve(to.fullPath), replace: true };
  });
}

function createRouterGuard(router: Router) {
  setupCommonGuard(router);
  setupAccessGuard(router);
}

export { createRouterGuard };
