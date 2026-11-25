import type {
  ComponentRecordType,
  GenerateMenuAndRoutesOptions,
} from '@vben/types';

import { generateAccessible } from '@vben/access';

import { message } from 'antdv-next';

import { getAllMenusApi } from '#/api';
import { BasicLayout, IFrameView } from '#/layouts';
import { $t } from '#/locales';

const forbiddenComponent = () => import('#/views/_core/fallback/forbidden.vue');

async function generateAccess(options: GenerateMenuAndRoutesOptions) {
  const pageMap: ComponentRecordType = import.meta.glob('../views/**/*.vue');

  const layoutMap: ComponentRecordType = {
    BasicLayout,
    IFrameView,
  };

  return await generateAccessible('mixed', {
    ...options,
    fetchMenuListAsync: async () => {
      message.loading({
        content: `${$t('common.loadingMenu')}...`,
        duration: 1.5,
      });
      const reservedNames = new Set([...options.router.getRoutes().map((route) => route.name), 'Dashboard', 'Workspace', 'Profile']);
      type Menus = Awaited<ReturnType<typeof getAllMenusApi>>;
      function businessRoutes(routes: Menus): Menus {
        return routes.filter((route) => !reservedNames.has(route.name) &&
          !['/', '/workspace', '/profile', '/dashboard', '/auth'].includes(route.path) &&
          !route.path.startsWith('/auth/')).map((route) => ({
          ...route,
          children: route.children ? businessRoutes(route.children) : undefined,
        }));
      }
      return businessRoutes(await getAllMenusApi());
    },
    // 可以指定没有权限跳转403页面
    forbiddenComponent,
    // 如果 route.meta.menuVisibleWithForbidden = true
    layoutMap,
    pageMap,
  });
}

export { generateAccess };
