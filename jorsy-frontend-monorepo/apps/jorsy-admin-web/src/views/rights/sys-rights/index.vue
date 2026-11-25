<script setup lang="ts">
import type { RightsRow } from '#/api/rights';

import { shallowRef } from 'vue';

import { Page } from '@vben/common-ui';
import { Button } from 'antdv-next';

import {
  orgApi,
  rightsApi,
  roleApi,
  setRightsEnabled,
  userApi,
} from '#/api/rights';
import AdminRecords from '#/views/_admin/admin-records.vue';
import { input, select, toggle } from '#/views/_admin/fields';
import { treeOptions } from '#/views/_admin/tree-options';
import {
  adminMutation,
  useAdminActions,
} from '#/views/_admin/use-admin-actions';

const { can } = useAdminActions('SysRights');
const userAccess = useAdminActions('SysUser');
const orgAccess = useAdminActions('SysOrg');
const roleAccess = useAdminActions('SysRole');
const scopes = [
  { value: '1', label: '本人' },
  { value: '2', label: '本组织' },
  { value: '3', label: '本组织及下级' },
  { value: '9', label: '全部' },
];
const yesNo = [
  { label: '是', value: true },
  { label: '否', value: false },
];
const filters = [
  select('enabled', '是否启用', yesNo),
  select('defaulted', '是否默认', yesNo),
];
const columns = [
  {
    field: 'sysUserName',
    title: '用户',
    minWidth: 130,
    formatter: ({ row }: { row: RightsRow }) =>
      row.sysUserName || row.sysUserCode,
  },
  {
    field: 'sysOrgName',
    title: '组织',
    minWidth: 150,
    formatter: ({ row }: { row: RightsRow }) =>
      row.sysOrgName || row.sysOrgCode,
  },
  {
    field: 'sysRoleName',
    title: '角色',
    minWidth: 146,
    formatter: ({ row }: { row: RightsRow }) =>
      row.sysRoleName || row.sysRoleCode,
  },
  {
    field: 'dataScope',
    title: '数据范围',
    width: 125,
    formatter: ({ cellValue }: { cellValue: string }) =>
      scopes.find(({ value }) => value === cellValue)?.label ?? cellValue,
  },
  {
    field: 'enabled',
    title: '启用',
    width: 76,
    formatter: ({ cellValue }: { cellValue: boolean }) =>
      cellValue ? '是' : '否',
  },
  {
    field: 'defaulted',
    title: '默认',
    width: 76,
    formatter: ({ cellValue }: { cellValue: boolean }) =>
      cellValue ? '是' : '否',
  },
  {
    field: 'extraOrgNames',
    title: '额外组织',
    minWidth: 140,
    formatter: ({ row }: { row: RightsRow }) =>
      row.extraOrgNames?.join('、') || '—',
  },
];

async function editor(row?: RightsRow, _parent?: RightsRow, readOnly = false) {
  if (readOnly && row)
    return {
      schema: [
        input('userText', '用户'),
        input('orgText', '组织'),
        input('roleText', '角色'),
        select('dataScope', '数据范围', scopes),
        toggle('enabled', '启用'),
        toggle('defaulted', '默认身份'),
        input('extraText', '额外组织'),
      ],
      initial: {
        userText: row.sysUserName || row.sysUserCode,
        orgText: row.sysOrgName || row.sysOrgCode,
        roleText: row.sysRoleName || row.sysRoleCode,
        extraText: row.extraOrgNames?.join('、') || '',
      },
    };

  const users = shallowRef<{ label: string; value: string }[]>([]);
  let searchVersion = 0;
  async function searchUsers(search: string) {
    const version = ++searchVersion;
    const result = userAccess.can('View')
      ? await userApi.page({
          currentPage: 1,
          pageSize: 20,
          sysUserCode: search || undefined,
        })
      : { items: [] };
    if (version !== searchVersion) return;
    const options = result.items.map((user) => ({
      label: `${user.sysUserName}（${user.sysUserCode}）`,
      value: user.sysUserId,
    }));
    if (row && !options.some(({ value }) => value === row.sysUserId)) {
      options.unshift({
        label: row.sysUserName || row.sysUserCode,
        value: row.sysUserId,
      });
    }
    users.value = options;
  }

  const [organizations, roles] = await Promise.all([
    orgAccess.can('View') ? orgApi.tree() : Promise.resolve([]),
    roleAccess.can('View') ? roleApi.tree() : Promise.resolve([]),
    searchUsers(''),
  ]);
  const orgChoices = treeOptions(
    organizations,
    (org) => org.sysOrgId,
    (org) => org.sysOrgName,
  );
  const roleChoices = treeOptions(
    roles,
    (role) => role.sysRoleId,
    (role) => role.sysRoleName,
  );
  if (!orgChoices.length && row) {
    orgChoices.push({
      value: row.sysOrgId,
      label: row.sysOrgName || row.sysOrgCode,
    });
    row.extraOrgIds?.forEach((id, i) => {
      if (id !== row.sysOrgId)
        orgChoices.push({ value: id, label: row.extraOrgNames?.[i] || id });
    });
  }
  if (!roleChoices.length && row)
    roleChoices.push({
      value: row.sysRoleId,
      label: row.sysRoleName || row.sysRoleCode,
    });

  return {
    schema: [
      {
        component: 'Select' as const,
        fieldName: 'sysUserId',
        label: '用户',
        rules: 'selectRequired' as const,
        componentProps: () => ({
          options: users.value,
          showSearch: true,
          filterOption: false,
          placeholder: '按登录账号搜索',
          onSearch: (query: string) => {
            void searchUsers(query).catch(() => {});
          },
        }),
      },
      {
        component: 'TreeSelect' as const,
        fieldName: 'sysOrgId',
        label: '组织',
        rules: 'selectRequired' as const,
        componentProps: {
          treeData: orgChoices,
          showSearch: true,
          treeNodeFilterProp: 'label',
        },
      },
      {
        component: 'TreeSelect' as const,
        fieldName: 'sysRoleId',
        label: '角色',
        rules: 'selectRequired' as const,
        componentProps: {
          treeData: roleChoices,
          showSearch: true,
          treeNodeFilterProp: 'label',
        },
      },
      select('dataScope', '数据范围', scopes, true),
      toggle(
        'enabled',
        '启用',
        true,
        !!row && !can(row.enabled ? 'Disable' : 'Enable'),
      ),
      toggle('defaulted', '默认身份'),
      {
        component: 'TreeSelect' as const,
        fieldName: 'extraOrgIds',
        label: '额外组织',
        formItemClass: 'md:col-span-2',
        componentProps: {
          treeData: orgChoices,
          multiple: true,
          allowClear: true,
          showSearch: true,
          treeNodeFilterProp: 'label',
        },
      },
    ],
    initial: row ? undefined : { dataScope: '1', extraOrgIds: [] },
  };
}

function toggleEnabled(row: RightsRow, refresh: () => Promise<unknown>) {
  adminMutation(
    `${row.enabled ? '停用' : '启用'}此权限身份？`,
    () => setRightsEnabled(row, !row.enabled),
    refresh,
  );
}
</script>

<template>
  <Page title="权限身份">
    <AdminRecords
      :api="rightsApi"
      :columns="columns"
      :editor="editor"
      :filters="filters"
      :editable="(row) => !row.sysData"
      :removable="(row) => !row.sysData"
      label="权限身份"
      permission="SysRights"
    >
      <template #actions="{ row, refresh }">
        <Button
          v-if="!row.sysData && can(row.enabled ? 'Disable' : 'Enable')"
          size="small"
          type="link"
          @click="toggleEnabled(row, refresh)"
          >{{ row.enabled ? '停用' : '启用' }}</Button
        >
      </template>
    </AdminRecords>
  </Page>
</template>
