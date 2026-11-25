<script setup lang="ts">
import type { SerialRow } from '#/api/basedata';

import { Page } from '@vben/common-ui';

import { serialApi } from '#/api/basedata';
import AdminRecords from '#/views/_admin/admin-records.vue';
import { dateRange, input, numeric } from '#/views/_admin/fields';

const filters = [
  input('serialCode', '规则代码'),
  input('serialName', '规则名称'),
  ...dateRange(),
];
const columns = [
  { field: 'serialCode', title: '规则代码', minWidth: 140 },
  { field: 'serialName', title: '规则名称', minWidth: 144 },
  { field: 'serialPrefix', title: '前缀', minWidth: 100 },
  { field: 'serialSuffix', title: '后缀', minWidth: 100 },
  { field: 'serialDigit', title: '序号位数', width: 100 },
  { field: 'dateFormat', title: '日期格式', minWidth: 130 },
  { field: 'updateTime', title: '修改时间', width: 168 },
];

const editor = (row?: SerialRow) => ({
  schema: [
    ...(row ? [] : [input('serialCode', '规则代码', 50, true)]),
    input('serialName', '规则名称', 100, true),
    input('serialPrefix', '前缀', 50),
    input('serialSuffix', '后缀', 50),
    numeric('serialDigit', '序号位数', 1, 18),
    input('dateFormat', '日期格式', 50),
  ],
  initial: row ? undefined : { serialDigit: 6 },
});
</script>

<template>
  <Page title="流水规则">
    <AdminRecords
      :api="serialApi"
      :columns="columns"
      :editor="editor"
      :filters="filters"
      :removable="(row) => !row.sysData"
      :row-label="(row) => row.serialCode"
      label="流水规则"
      permission="SysSerial"
    />
  </Page>
</template>
