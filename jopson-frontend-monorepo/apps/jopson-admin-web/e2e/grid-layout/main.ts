import { createApp } from 'vue';
import { createMemoryHistory, createRouter } from 'vue-router';

import { initTippy } from '@vben/common-ui/es/tippy';
import { initPreferences } from '@vben/preferences';
import { initStores } from '@vben/stores';
import { unmountGlobalLoading } from '@vben/utils';
import '@vben/styles';
import '@vben/styles/antdv-next';

import { initComponentAdapter } from '#/adapter/component';
import { initSetupVbenForm } from '#/adapter/form';
import App from '#/app.vue';
import { setupI18n } from '#/locales';

async function mountTestPage() {
  const namespace = 'grid-layout-test';
  await initPreferences({
    namespace,
    overrides: { app: { name: 'Jopson Admin' } },
  });
  await initComponentAdapter();
  await initSetupVbenForm();
  const app = createApp(App);
  await setupI18n(app);
  await initStores(app, { namespace });
  initTippy(app);
  app.use(
    createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/',
          component: () => import('#/layouts/basic.vue'),
          meta: { title: '测试' },
          children: [
            {
              path: '',
              name: 'TestList',
              meta: { title: '列表' },
              component: () => import('./list.vue'),
            },
          ],
        },
      ],
    }),
  );
  app.mount('#app');
  unmountGlobalLoading();
}

void mountTestPage();
