<script setup lang="ts">
import type { VbenFormSchema } from '#/adapter/form';
import { Page } from '@vben/common-ui';
import { preferences, updatePreferences } from '@vben/preferences';
import { useVbenVxeGrid } from '#/adapter/vxe-table';

const mode = new URLSearchParams(location.search).get('mode');
const fields: VbenFormSchema[] = [
  { component: 'Input', fieldName: 'keyword', label: '关键字' },
  { component: 'Input', fieldName: 'number', label: '业务单据编号' },
  { component: 'Input', fieldName: 'owner', label: '经办人' },
  { component: 'Input', fieldName: 'status', label: '当前业务办理状态' },
  {
    component: 'RangePicker',
    fieldName: 'period',
    label: '日期范围',
    formItemClass: 'md:col-span-2',
  },
];

const schema =
  mode === 'three'
    ? fields.slice(0, 3)
    : fields.filter(
        (field) =>
          mode !== 'span' || !['owner', 'status'].includes(field.fieldName),
      );

const [Grid] = useVbenVxeGrid({
  formOptions:
    mode === 'bare'
      ? undefined
      : {
          schema,
          ...(mode === 'narrow' && { wrapperClass: 'grid-cols-2' }),
          ...(mode === 'expanded' && { collapsed: false }),
        },
  gridOptions: {
    columns: [
      { type: 'checkbox' },
      { type: 'seq', title: '序号' },
      { field: 'number', title: '单号', minWidth: 140 },
      { field: 'amount', title: '金额', align: 'right', headerAlign: 'right' },
      {
        field: 'action',
        title: '操作',
        width: 116,
        slots: { default: 'action' },
      },
    ],
    pagerConfig: { enabled: true },
    proxyConfig: {
      ajax: {
        query: async (
          { page }: { page: { currentPage: number; pageSize: number } },
          values: { keyword?: string },
        ) => {
          const params = new URLSearchParams({
            currentPage: String(page.currentPage),
            pageSize: String(page.pageSize),
            keyword: values.keyword ?? '',
          });
          const response = await fetch(`/__grid-test/records?${params}`);
          return (await response.json()).data;
        },
      },
    },
  },
});
</script>

<template>
  <Page>
    <div :style="mode === 'narrow' ? { maxWidth: '620px' } : undefined">
      <Grid>
        <template #toolbar-actions>
          <button
            type="button"
            @click="
              updatePreferences({
                sidebar: { collapsed: !preferences.sidebar.collapsed },
              })
            "
          >
            切换测试侧栏
          </button>
        </template>
        <template #action><button type="button">查看单据</button></template>
      </Grid>
    </div>
  </Page>
</template>
