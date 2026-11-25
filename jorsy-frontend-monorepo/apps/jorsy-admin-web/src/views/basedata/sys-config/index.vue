<script setup lang="ts">
import { Page } from '@vben/common-ui';

import { configApi } from '#/api/basedata';
import AdminRecords from '#/views/_admin/admin-records.vue';
import { dateRange, input, textarea } from '#/views/_admin/fields';

const filters = [
  input('sysConfigCode', '配置代码'),
  input('sysConfigName', '配置名称'),
  ...dateRange(),
];
const columns = [
  { field: 'sysConfigCode', title: '配置代码', minWidth: 160 },
  { field: 'sysConfigName', title: '配置名称', minWidth: 160 },
  { field: 'sysConfigValue', title: '配置值', minWidth: 180 },
  { field: 'remark', title: '备注', minWidth: 130 },
  { field: 'updateTime', title: '修改时间', width: 168 },
];
const editor = () => ({
  schema: [
    input('sysConfigCode', '配置代码', 100, true),
    input('sysConfigName', '配置名称', 100, true),
    textarea('sysConfigValue', '配置值', 1000),
    textarea('remark', '备注', 500),
  ],
});
</script>

<template>
  <Page title="系统配置">
    <AdminRecords
      :api="configApi"
      :columns="columns"
      :editor="editor"
      :filters="filters"
      :row-label="(row) => row.sysConfigCode"
      label="配置"
      permission="SysConfig"
    />
  </Page>
</template>
