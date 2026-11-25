<script setup lang="ts">
import type { OrgRow } from '#/api/rights';

import { Page } from '@vben/common-ui';

import { orgApi } from '#/api/rights';
import AdminRecords from '#/views/_admin/admin-records.vue';
import { input, numeric, select } from '#/views/_admin/fields';

const orgTypes = [
  { label: '公司', value: 'C' },
  { label: '部门', value: 'D' },
  { label: '小组', value: 'G' },
];
const columns = [
  { field: 'sysOrgName', title: '组织名称', minWidth: 230, treeNode: true },
  { field: 'sysOrgCode', title: '组织编码', minWidth: 160 },
  {
    field: 'sysOrgType',
    title: '组织类型',
    width: 100,
    formatter: ({ cellValue }: { cellValue: string }) =>
      orgTypes.find(({ value }) => value === cellValue)?.label ?? cellValue,
  },
  { field: 'sortNo', title: '排序', width: 90 },
];

function editor(row?: OrgRow, parent?: OrgRow) {
  return {
    schema: [
      ...(parent
        ? [
            {
              ...input('parentName', '上级组织'),
              componentProps: { disabled: true },
            },
          ]
        : []),
      input('sysOrgName', '组织名称', 100, true),
      select('sysOrgType', '组织类型', orgTypes, true),
      numeric('sortNo', '排序'),
    ],
    initial: parent ? { parentName: parent.sysOrgName } : undefined,
    body: ({ parentName: _parentName, ...values }: Record<string, unknown>) =>
      row ? values : { ...values, parentId: parent?.sysOrgId ?? null },
  };
}
</script>

<template>
  <Page title="组织管理">
    <AdminRecords
      :api="orgApi"
      :columns="columns"
      :editor="editor"
      :root-create="false"
      :child="() => true"
      :removable="
        (row) => !!row.parentId && !row.sysData && !row.children.length
      "
      :row-label="(row) => row.sysOrgName"
      tree
      label="组织"
      permission="SysOrg"
    />
  </Page>
</template>
