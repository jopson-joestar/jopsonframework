<script setup lang="ts">
import type { VbenFormSchema } from '#/adapter/form';
import type { FormValues } from '#/api/admin-records';
import type { ResourceRow } from '#/api/rights';

import { ref, shallowRef } from 'vue';

import { Page } from '@vben/common-ui';
import { Button } from 'antdv-next';

import { z } from '#/adapter/form';
import { resourceApi } from '#/api/rights';
import AdminRecords from '#/views/_admin/admin-records.vue';
import {
  input,
  numeric,
  parseJsonValue,
  select,
  textarea,
} from '#/views/_admin/fields';
import { treeOptions } from '#/views/_admin/tree-options';
import { useAdminActions } from '#/views/_admin/use-admin-actions';

import { resourceTypes } from './resource-file';
import ResourceImport from './resource-import.vue';

const { can } = useAdminActions('SysRes');
const resources = shallowRef<ResourceRow[]>([]);
const showImport = ref(false);
let refreshResources: (() => Promise<unknown>) | undefined;
const title = (row: ResourceRow) =>
  String(row.routeMeta?.title ?? row.routeName);
const columns = [
  {
    field: 'routeName',
    title: '名称',
    minWidth: 195,
    treeNode: true,
    formatter: ({ row }: { row: ResourceRow }) => title(row),
  },
  {
    field: 'sysResType',
    title: '类型',
    width: 92,
    formatter: ({ cellValue }: { cellValue: string }) =>
      resourceTypes.find(({ value }) => value === cellValue)?.label ??
      cellValue,
  },
  { field: 'routePath', title: '路由路径', minWidth: 176 },
  { field: 'permission', title: '权限标识', minWidth: 180 },
  { field: 'sortNo', title: '排序', width: 74 },
];
const isBranch = (row: ResourceRow) =>
  ['FOLDER', 'MENU'].includes(row.sysResType);
function filterBranches(rows: ResourceRow[]): ResourceRow[] {
  return rows
    .filter(isBranch)
    .map((row) => ({ ...row, children: filterBranches(row.children) }));
}

function visible(
  field: VbenFormSchema,
  types: string[],
  required = false,
): VbenFormSchema {
  return {
    ...field,
    dependencies: {
      triggerFields: ['sysResType'],
      show: (values) => types.includes(values.sysResType),
      required: (values) => required && types.includes(values.sysResType),
    },
  };
}

function body(values: FormValues) {
  const { title, icon, iframeSrc, link, metaJson, ...fields } = values;
  const extra = parseJsonValue(metaJson, '扩展路由配置');
  if (extra !== null && (typeof extra !== 'object' || Array.isArray(extra)))
    throw new Error('扩展路由配置须为 JSON 对象');
  const routeMeta = {
    ...extra,
    title,
    icon: icon || undefined,
    iframeSrc: iframeSrc || undefined,
    link: link || undefined,
  };
  return { ...fields, routeMeta, parentId: fields.parentId || null };
}

function editor(row?: ResourceRow, parent?: ResourceRow) {
  const { title, icon, iframeSrc, link, ...extra } = row?.routeMeta ?? {};
  return {
    schema: [
      select('sysResType', '类型', resourceTypes, true),
      {
        component: 'TreeSelect' as const,
        fieldName: 'parentId',
        label: '上级资源',
        componentProps: {
          allowClear: true,
          treeData: treeOptions(
            filterBranches(resources.value),
            (item) => item.sysResId,
            (item) => String(item.routeMeta?.title ?? item.routeName),
            row?.sysResId,
          ),
        },
      },
      input('title', '显示名称', 100, true),
      {
        ...input('routeName', '路由名称', 100, true),
        rules: z
          .string()
          .max(100)
          .regex(/^[A-Z][A-Za-z0-9]*$/, '请输入 PascalCase 路由名'),
      },
      visible(
        input('routePath', '路由路径', 500),
        ['FOLDER', 'MENU', 'IFRAME', 'LINK'],
        true,
      ),
      visible(input('component', 'Vue 组件', 500), ['MENU'], true),
      visible(input('redirect', '重定向路径', 500), ['FOLDER', 'MENU']),
      input('permission', '权限标识', 100),
      visible(input('iframeSrc', '内嵌页地址', 500), ['IFRAME'], true),
      visible(input('link', '外链地址', 500), ['LINK'], true),
      visible(input('icon', '图标', 100), ['FOLDER', 'MENU', 'IFRAME', 'LINK']),
      numeric('sortNo', '排序'),
      textarea('metaJson', '扩展路由配置（JSON）'),
      textarea('remark', '备注', 500),
    ],
    initial: {
      title,
      icon,
      iframeSrc,
      link,
      metaJson: Object.keys(extra).length ? JSON.stringify(extra, null, 2) : '',
      ...(row ? {} : { sysResType: 'MENU', parentId: parent?.sysResId }),
    },
    body,
  };
}

function openImport(refresh: () => Promise<unknown>) {
  refreshResources = refresh;
  showImport.value = true;
}
</script>

<template>
  <Page title="资源管理">
    <AdminRecords
      :api="resourceApi"
      :columns="columns"
      :editor="editor"
      :child="isBranch"
      :removable="(row) => !row.children.length"
      :row-label="title"
      :on-loaded="(rows) => (resources = rows)"
      tree
      label="资源"
      permission="SysRes"
    >
      <template #toolbar="{ refresh }">
        <Button v-if="can('Import')" @click="openImport(refresh)"
          >导入 JSON</Button
        >
      </template>
    </AdminRecords>
    <ResourceImport
      v-model:open="showImport"
      @complete="refreshResources?.()"
    />
  </Page>
</template>
