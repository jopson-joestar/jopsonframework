<script setup lang="ts">
import type { UserRow, UserStatus } from '#/api/rights';

import { Page } from '@vben/common-ui';
import { Button } from 'antdv-next';

import { z } from '#/adapter/form';
import { changeUserStatus, userApi } from '#/api/rights';
import { useAuthStore } from '#/store/auth';
import AdminRecords from '#/views/_admin/admin-records.vue';
import { dateRange, input, select } from '#/views/_admin/fields';
import {
  adminMutation,
  useAdminActions,
} from '#/views/_admin/use-admin-actions';

const auth = useAuthStore();
const { can } = useAdminActions('SysUser');
const statuses = [
  { label: '未审核', value: 0 },
  { label: '审核通过', value: 10 },
  { label: '锁定', value: 1 },
  { label: '作废', value: -100 },
];
const filters = [
  input('sysUserCode', '登录账号'),
  input('sysUserName', '用户名称'),
  select('sysUserStatus', '状态', statuses),
  ...dateRange(),
];
const columns = [
  { field: 'sysUserCode', title: '登录账号', minWidth: 142 },
  { field: 'sysUserName', title: '用户名称', minWidth: 142 },
  {
    field: 'sysUserStatus',
    title: '状态',
    width: 100,
    formatter: ({ cellValue }: { cellValue: number }) =>
      statuses.find(({ value }) => value === cellValue)?.label ?? cellValue,
  },
  { field: 'sysUserMobile', title: '手机号', minWidth: 124 },
  { field: 'sysUserEmail', title: '邮箱', minWidth: 166 },
  { field: 'createTime', title: '创建时间', width: 168 },
];
const editor = (row?: UserRow) => ({
  schema: [
    ...(row
      ? []
      : [
          input('sysUserCode', '登录账号', 50, true),
          {
            ...input('initialPassword', '初始密码'),
            component: 'InputPassword' as const,
            rules: z
              .string()
              .min(1, '请输入初始密码')
              .refine(
                (value) => new TextEncoder().encode(value).length <= 72,
                '初始密码最多 72 个 UTF-8 字节',
              ),
          },
        ]),
    input('sysUserName', '用户名称', 100, true),
    input('sysUserMobile', '手机号', 30),
    input('sysUserEmail', '邮箱', 100),
    input('sysUserQq', 'QQ 号', 30),
  ],
});

function protectedUser(row: UserRow) {
  return row.sysData || row.sysUserId === auth.user?.userId;
}

function setStatus(
  row: UserRow,
  status: UserStatus,
  label: string,
  refresh: () => Promise<unknown>,
) {
  adminMutation(
    `${label}账号「${row.sysUserCode}」？`,
    () => changeUserStatus(row, status),
    refresh,
  );
}
</script>

<template>
  <Page title="用户管理">
    <AdminRecords
      :api="userApi"
      :columns="columns"
      :editor="editor"
      :filters="filters"
      :editable="(row) => row.sysUserStatus === 0"
      :removable="
        (row) => !protectedUser(row) && [0, -100].includes(row.sysUserStatus)
      "
      :row-label="(row) => row.sysUserCode"
      label="用户"
      permission="SysUser"
    >
      <template #actions="{ row, refresh }">
        <Button
          v-if="row.sysUserStatus === 0 && can('Audit')"
          size="small"
          type="link"
          @click="setStatus(row, 10, '审核', refresh)"
          >审核</Button
        >
        <Button
          v-if="
            row.sysUserStatus === 10 &&
            !protectedUser(row) &&
            can('CancelAudit')
          "
          size="small"
          type="link"
          @click="setStatus(row, 0, '撤审', refresh)"
          >撤审</Button
        >
        <Button
          v-if="
            row.sysUserStatus === 10 && !protectedUser(row) && can('Disable')
          "
          size="small"
          type="link"
          @click="setStatus(row, 1, '停用', refresh)"
          >停用</Button
        >
        <Button
          v-if="row.sysUserStatus === 1 && can('Enable')"
          size="small"
          type="link"
          @click="setStatus(row, 10, '启用', refresh)"
          >启用</Button
        >
      </template>
    </AdminRecords>
  </Page>
</template>
