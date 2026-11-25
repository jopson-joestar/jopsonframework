<script setup lang="ts">
import type { SyncOperation } from '#/api/admin-records';
import type { ResourceImportNode } from '#/api/rights';

import { computed, ref, shallowRef } from 'vue';

import { Alert, CheckboxGroup, message, Modal, RadioGroup } from 'antdv-next';

import { importResources } from '#/api/rights';

import { parseResourceFile } from './resource-file';

const open = defineModel<boolean>('open', { required: true });
const emit = defineEmits<{ complete: [] }>();
const mode = ref<'FULL' | 'INCREMENTAL'>('INCREMENTAL');
const operations = ref<SyncOperation[]>(['ADD', 'UPDATE']);
const modeOptions = [
  { label: '增量', value: 'INCREMENTAL' },
  { label: '全量替换', value: 'FULL' },
];
const operationOptions = [
  { label: '新增', value: 'ADD' },
  { label: '更新', value: 'UPDATE' },
  { label: '删除', value: 'DELETE' },
];
const source = shallowRef<{
  name: string;
  resources: ResourceImportNode[];
  count: number;
}>();
const errorText = ref('');
const reading = ref(false);
const submitting = ref(false);
const destructive = computed(
  () => mode.value === 'FULL' || operations.value.includes('DELETE'),
);
const ready = computed(
  () =>
    !!source.value &&
    !reading.value &&
    (mode.value === 'FULL' || !!operations.value.length),
);
let fileVersion = 0;

function reset() {
  fileVersion++;
  source.value = undefined;
  mode.value = 'INCREMENTAL';
  operations.value = ['ADD', 'UPDATE'];
  errorText.value = '';
  reading.value = false;
}

async function readFile(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0];
  const version = ++fileVersion;
  source.value = undefined;
  errorText.value = '';
  if (!file) return;
  reading.value = true;
  try {
    const parsed = parseResourceFile(await file.text());
    if (version === fileVersion) source.value = { name: file.name, ...parsed };
  } catch (error) {
    if (version === fileVersion)
      errorText.value =
        error instanceof Error ? error.message : '无法读取菜单文件';
  } finally {
    if (version === fileVersion) reading.value = false;
  }
}

async function submit() {
  if (!ready.value || !source.value || submitting.value) return;
  submitting.value = true;
  try {
    const report = await importResources(
      mode.value,
      mode.value === 'FULL' ? [] : operations.value,
      source.value.resources,
    );
    message.success(
      `新增 ${report.addedCount} 项，更新 ${report.updatedCount} 项，删除 ${report.deletedCount} 项`,
    );
    open.value = false;
    emit('complete');
  } finally {
    submitting.value = false;
  }
}

function confirmImport() {
  if (!ready.value || submitting.value) return;
  if (!destructive.value) {
    void submit().catch(() => {});
    return;
  }
  Modal.confirm({
    title:
      mode.value === 'FULL' ? '确认替换整棵资源树？' : '确认删除清单外的资源？',
    content:
      mode.value === 'FULL'
        ? '现有资源及其角色授权将全部清除，再导入文件中的资源。'
        : '文件未包含的现有资源及其角色授权将被移除。',
    okType: 'danger',
    okText: '确认导入',
    cancelText: '返回检查',
    onOk: submit,
  });
}
</script>

<template>
  <Modal
    v-model:open="open"
    title="导入菜单资源"
    ok-text="导入"
    cancel-text="取消"
    :confirm-loading="submitting"
    :closable="!submitting"
    :mask-closable="false"
    :cancel-button-props="{ disabled: submitting }"
    :ok-button-props="{ disabled: !ready, danger: destructive }"
    @after-close="reset"
    @ok="confirmImport"
  >
    <div class="flex flex-col gap-4">
      <RadioGroup
        v-model:value="mode"
        :options="modeOptions"
        :disabled="submitting"
      />
      <CheckboxGroup
        v-if="mode === 'INCREMENTAL'"
        v-model:value="operations"
        :options="operationOptions"
        :disabled="submitting"
      />
      <label class="flex flex-col gap-2"
        >资源 JSON 文件
        <input
          v-if="open"
          type="file"
          accept=".json,application/json"
          :disabled="submitting"
          @change="readFile"
        />
      </label>
      <Alert v-if="errorText" type="error" :message="errorText" show-icon />
      <p v-else-if="source">{{ source.name }}，共 {{ source.count }} 项资源</p>
      <Alert
        v-if="destructive"
        type="warning"
        message="此操作会移除资源及对应的角色授权。"
        show-icon
      />
    </div>
  </Modal>
</template>
