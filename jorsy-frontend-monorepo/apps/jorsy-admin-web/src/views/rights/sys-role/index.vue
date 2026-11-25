<script setup lang="ts">
import type { RoleRow } from '#/api/rights';

import { shallowRef } from 'vue';
import { useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { Button } from 'antdv-next';

import { roleApi } from '#/api/rights';
import AdminRecords from '#/views/_admin/admin-records.vue';
import { input, textarea } from '#/views/_admin/fields';
import { treeOptions } from '#/views/_admin/tree-options';
import { useAdminActions } from '#/views/_admin/use-admin-actions';

const router = useRouter();
const authorization = useAdminActions('SysRoleAuth');
const roles = shallowRef<RoleRow[]>([]);
const columns = [
  { field: 'sysRoleName', title: '角色名称', minWidth: 220, treeNode: true },
  { field: 'sysRoleCode', title: '角色代码', minWidth: 150 },
  { field: 'remark', title: '备注', minWidth: 180 },
];

function editor(row?: RoleRow, parent?: RoleRow) {
  return {
    schema: [
      {
        component: 'TreeSelect' as const,
        fieldName: 'parentId',
        label: '上级角色',
        rules: row?.sysData ? undefined : ('selectRequired' as const),
        componentProps: {
          treeData: treeOptions(
            roles.value,
            (item) => item.sysRoleId,
            (item) => item.sysRoleName,
            row?.sysRoleId,
          ),
        },
      },
      input('sysRoleCode', '角色代码', 50, true),
      input('sysRoleName', '角色名称', 100, true),
      textarea('remark', '备注', 500),
    ],
    initial: parent ? { parentId: parent.sysRoleId } : undefined,
  };
}
</script>

<template>
  <Page title="角色管理">
    <AdminRecords
      :api="roleApi"
      :columns="columns"
      :editor="editor"
      :root-create="false"
      :child="() => true"
      :editable="(row) => !row.sysData"
      :removable="(row) => !row.sysData && !row.children.length"
      :on-loaded="(rows) => (roles = rows)"
      :row-label="(row) => row.sysRoleName"
      tree
      label="角色"
      permission="SysRole"
    >
      <template #actions="{ row }">
        <Button
          v-if="authorization.can('View')"
          size="small"
          type="link"
          @click="
            router.push({
              path: '/rights/sys-role-auth',
              query: { role: row.sysRoleId },
            })
          "
          >授权</Button
        >
      </template>
    </AdminRecords>
  </Page>
</template>
