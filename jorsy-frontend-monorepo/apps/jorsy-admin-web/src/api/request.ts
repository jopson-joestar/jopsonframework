import { useAppConfig } from '@vben/hooks';
import { preferences } from '@vben/preferences';
import { defaultResponseInterceptor, errorMessageResponseInterceptor, RequestClient } from '@vben/request';
import { message } from 'antdv-next';

import { useAuthStore } from '#/store';

const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);
export const requestClient = new RequestClient({ baseURL: apiURL, responseReturn: 'data' });
type Csrf = { headerName: string; token: string };
let csrf: Promise<Csrf> | undefined;

export function refreshCsrf() {
  csrf = requestClient.get<Csrf>('/auth/csrf').catch((error) => {
    csrf = undefined;
    throw error;
  });
  return csrf;
}

requestClient.addRequestInterceptor({
  fulfilled: async (config) => {
    config.headers['Accept-Language'] = preferences.app.locale;
    if (!['get', 'head', 'options'].includes(config.method?.toLowerCase() ?? 'get')) {
      const token = await (csrf ?? refreshCsrf());
      config.headers[token.headerName] = token.token;
    }
    return config;
  },
});

requestClient.addResponseInterceptor(defaultResponseInterceptor({
  codeField: 'code', dataField: 'data', successCode: 0,
}));
requestClient.addResponseInterceptor(errorMessageResponseInterceptor((fallback, error) => {
  const status = error.response?.status;
  const path = error.config?.url;
  if (status === 401 && path !== '/auth/login') {
    csrf = undefined;
    useAuthStore().expired();
    return;
  }
  if (status === 403) csrf = undefined;
  message.error(error.response?.data?.message ?? fallback);
}));
