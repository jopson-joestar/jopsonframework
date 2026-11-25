<script setup lang="ts">
import type { SyncOperation } from '#/api/admin-records';
import type { DictItemRow, DictRow } from '#/api/basedata';

import { computed, shallowRef, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { Button, CheckboxGroup, Empty, message, Modal, Tag } from 'antdv-next';

import { dictApi, dictItemApi, syncDictionary } from '#/api/basedata';
import AdminRecords from '#/views/_admin/admin-records.vue';
import {
  input,
  numeric,
  parseJsonValue,
  select,
  textarea,
  toggle,
} from '#/views/_admin/fields';
import {
  adminMutation,
  useAdminActions,
} from '#/views/_admin/use-admin-actions';

const { can } = useAdminActions('SysDict');
const items = useAdminActions('SysDictItem');
const selected = shallowRef<DictRow>();
const showSync = ref(false);
const operations = ref<SyncOperation[]>([]);
let reloadDicts: (() => Promise<unknown>) | undefined;
const itemGrid = shallowRef<{ refresh: () => Promise<unknown> }>();

const dictColumns = [
  { field: 'sysDictCode', title: '字典代码', minWidth: 136 },
  { field: 'sysDictName', title: '字典名称', minWidth: 132 },
  {
    field: 'locked',
    title: '状态',
    width: 82,
    formatter: ({ cellValue }: { cellValue: boolean }) =>
      cellValue ? '锁定' : '普通',
  },
];
const itemColumns = [
  { field: 'dictItemValue', title: '明细值', minWidth: 105 },
  { field: 'dictItemLabel', title: '标签', minWidth: 110 },
  { field: 'sortNo', title: '排序', width: 72 },
  {
    field: 'disabled',
    title: '状态',
    width: 74,
    formatter: ({ cellValue }: { cellValue: boolean }) =>
      cellValue ? '禁用' : '启用',
  },
];
const dictFilters = [
  input('sysDictCode', '字典代码'),
  input('sysDictName', '字典名称'),
  select('locked', '状态', [
    { label: '锁定', value: true },
    { label: '普通', value: false },
  ]),
];
const itemFilters = [
  input('dictItemValue', '明细值'),
  input('dictItemLabel', '标签'),
];
const itemQuery = computed(() => ({ sysDictId: selected.value?.sysDictId }));
const syncOptions = [
  { label: '新增', value: 'ADD' },
  { label: '更新', value: 'UPDATE' },
  { label: '删除', value: 'DELETE' },
];

function dictEditor(row?: DictRow) {
  const code = input('sysDictCode', '字典代码', 50, true);
  if (row?.locked)
    code.componentProps = { ...code.componentProps, disabled: true };
  return {
    schema: [
      code,
      input('sysDictName', '字典名称', 100, true),
      textarea('remark', '备注', 500),
    ],
  };
}

function itemEditor(row?: DictItemRow) {
  const dictId = selected.value?.sysDictId;
  if (!dictId) throw new Error('请先选择字典');
  const value = input('dictItemValue', '明细值', 50, true);
  if (selected.value?.locked)
    value.componentProps = { ...value.componentProps, disabled: true };
  return {
    schema: [
      value,
      input('dictItemLabel', '标签', 100, true),
      numeric('sortNo', '排序'),
      toggle('disabled', '禁用'),
      input('labelColor', '标签颜色', 50),
      input('labelClass', '标签样式', 100),
      textarea('dictItemExtra', '扩展数据（JSON）'),
      textarea('remark', '备注', 500),
    ],
    initial: {
      dictItemExtra:
        row?.dictItemExtra === null || row?.dictItemExtra === undefined
          ? ''
          : JSON.stringify(row.dictItemExtra, null, 2),
    },
    body: (values: Record<string, unknown>) => ({
      ...values,
      sysDictId: dictId,
      dictItemExtra: parseJsonValue(values.dictItemExtra, '扩展数据'),
    }),
  };
}

function loaded(rows: DictRow[]) {
  if (selected.value)
    selected.value = rows.find(
      (row) => row.sysDictId === selected.value?.sysDictId,
    );
}

function openSync(refresh: () => Promise<unknown>) {
  reloadDicts = refresh;
  operations.value = ['ADD', 'UPDATE'];
  showSync.value = true;
}

function runSync() {
  const requested = [...operations.value];
  if (!requested.length) return;
  adminMutation(
    requested.includes('DELETE')
      ? '同步枚举并删除其中已移除的字典项？'
      : '同步已登记的枚举字典？',
    async () => {
      const result = await syncDictionary(requested);
      message.info(
        `字典新增 ${result.createdDictCount} 项、更新 ${result.updatedDictCount} 项，明细新增 ${result.createdItemCount} 项、更新 ${result.updatedItemCount} 项、删除 ${result.deletedItemCount} 项`,
      );
      showSync.value = false;
    },
    async () => {
      await reloadDicts?.();
      await itemGrid.value?.refresh();
    },
  );
}
</script>

<template>
  <Page title="系统字典">
    <div class="grid items-start gap-4 xl:grid-cols-2">
      <AdminRecords
        :api="dictApi"
        :columns="dictColumns"
        :editor="dictEditor"
        :filters="dictFilters"
        :removable="(row) => !row.locked"
        :on-loaded="loaded"
        label="字典"
        permission="SysDict"
      >
        <template #toolbar="{ refresh }">
          <Button v-if="can('Sync')" @click="openSync(refresh)"
            >同步枚举</Button
          >
        </template>
        <template #actions="{ row }">
          <Button
            v-if="items.can('View')"
            size="small"
            :type="selected?.sysDictId === row.sysDictId ? 'primary' : 'link'"
            @click="selected = row"
            >字典项</Button
          >
        </template>
      </AdminRecords>
      <div
        v-if="selected && items.can('View')"
        :key="selected.sysDictId"
        class="min-w-0"
      >
        <div class="mb-2 flex items-center gap-2">
          <span class="font-medium">{{ selected.sysDictName }}</span>
          <Tag>{{ selected.sysDictCode }}</Tag>
        </div>
        <AdminRecords
          ref="itemGrid"
          :api="dictItemApi"
          :columns="itemColumns"
          :editor="itemEditor"
          :filters="itemFilters"
          :query="itemQuery"
          :root-create="!selected.locked"
          :removable="() => !selected?.locked"
          label="字典项"
          permission="SysDictItem"
        />
      </div>
      <Empty
        v-else-if="items.can('View')"
        class="rounded-md border py-12"
        description="选择一个字典查看明细"
      />
    </div>
    <Modal
      v-model:open="showSync"
      title="同步枚举字典"
      ok-text="继续"
      cancel-text="取消"
      :ok-button-props="{ disabled: operations.length === 0 }"
      @ok="runSync"
    >
      <CheckboxGroup v-model:value="operations" :options="syncOptions" />
    </Modal>
  </Page>
</template>
