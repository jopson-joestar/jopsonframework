import { useAccess } from '@vben/access';
import { message, Modal } from 'antdv-next';

import { useAuthStore } from '#/store/auth';

export function useAdminActions(resource: string) {
  const auth = useAuthStore();
  const { hasAccessByCodes } = useAccess();

  function can(action: string) {
    return (
      auth.user?.administrator ||
      hasAccessByCodes([`Admin:${resource}:${action}`])
    );
  }

  return { can };
}

export function isConflict(error: unknown) {
  if (typeof error !== 'object' || error === null) return false;
  return (
    ('code' in error && error.code === 409) ||
    ('response' in error &&
      (error.response as { status?: number })?.status === 409)
  );
}

export function adminMutation(
  title: string,
  work: () => Promise<unknown>,
  refresh: () => Promise<unknown>,
) {
  Modal.confirm({
    title,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await work();
      } catch (error) {
        if (!isConflict(error)) throw error;
        await refresh();
        return;
      }
      message.success('操作成功');
      await refresh();
    },
  });
}
