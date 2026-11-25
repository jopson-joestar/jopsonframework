<script setup lang="ts" generic="R extends VersionedRow">
import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type {
  FormValues,
  PageQuery,
  PageResult,
  RecordsApi,
  VersionedRow,
} from '#/api/admin-records';
import type { EditorForm } from './fields';

import { ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { Button, message, Space } from 'antdv-next';

import { useVbenForm } from '#/adapter/form';
import { useVbenVxeGrid } from '#/adapter/vxe-table';

import {
  adminMutation,
  isConflict,
  useAdminActions,
} from './use-admin-actions';

const props = withDefaults(
  defineProps<{
    api: RecordsApi<R>;
    permission: string;
    label: string;
    columns: NonNullable<VxeTableGridOptions<R>['columns']>;
    editor: (
      row?: R,
      parent?: R,
      readOnly?: boolean,
    ) => EditorForm | Promise<EditorForm>;
    filters?: VbenFormSchema[];
    query?: FormValues;
    tree?: boolean;
    child?: (row: R) => boolean;
    rootCreate?: boolean;
    editable?: (row: R) => boolean;
    removable?: (row: R) => boolean;
    rowLabel?: (row: R) => string;
    onLoaded?: (rows: R[]) => void;
  }>(),
  { rootCreate: true },
);

const { can } = useAdminActions(props.permission);
let latestQuery: Promise<PageResult<R>> | undefined;

function queryRows(page: PageQuery, values?: FormValues) {
  const request = props.tree
    ? props.api.tree().then((items) => ({
        items,
        total: items.length,
        currentPage: 1,
        pageSize: items.length,
      }))
    : props.api.page({
        currentPage: page.currentPage,
        pageSize: page.pageSize,
        ...props.query,
        ...values,
      });
  const pending: Promise<PageResult<R>> = request.then(
    (result) => {
      if (latestQuery && pending !== latestQuery) return latestQuery;
      props.onLoaded?.(result.items);
      return result;
    },
    (error) => {
      if (latestQuery && pending !== latestQuery) return latestQuery;
      throw error;
    },
  );
  latestQuery = pending;
  return pending;
}

const [Grid, gridApi] = useVbenVxeGrid<R>({
  formOptions: props.filters?.length ? { schema: props.filters } : undefined,
  gridOptions: {
    columns: [
      ...props.columns,
      {
        title: '操作',
        width: props.permission === 'SysUser' ? 270 : 212,
        fixed: 'right',
        slots: { default: 'actions' },
      },
    ],
    rowConfig: { keyField: props.api.idField },
    pagerConfig: {
      enabled: !props.tree,
      pageSize: 20,
      pageSizes: [20, 50, 100],
    },
    ...(props.tree && {
      treeConfig: { childrenField: 'children', expandAll: true },
    }),
    proxyConfig: {
      ajax: {
        query: ({ page }: { page: PageQuery }, values?: FormValues) =>
          queryRows(page, values),
      },
    },
    toolbarConfig: { refresh: true, custom: true },
  },
});

const refresh = () => gridApi.query();
defineExpose({ refresh });
const [Form, formApi] = useVbenForm({
  showDefaultActions: false,
  wrapperClass: 'grid-cols-1 md:grid-cols-2',
  layout: 'vertical',
});

const selected = ref<R>() as { value: R | undefined };
let currentForm: EditorForm;
let loadingEditor = false;
let saving = false;
const [Drawer, drawerApi] = useVbenDrawer({
  class: 'w-full sm:max-w-[720px]',
  closeOnClickModal: false,
  confirmText: '保存',
  cancelText: '关闭',
  onConfirm: save,
  async onClosed() {
    await formApi.resetForm();
    formApi.setState({ schema: [] });
    selected.value = undefined;
  },
});

async function openEditor(row?: R, parent?: R, readOnly = false) {
  if (loadingEditor || saving) return;
  loadingEditor = true;
  try {
    const detail = row ? await props.api.detail(row) : undefined;
    const locked = readOnly || (detail && props.editable?.(detail) === false);
    currentForm = await props.editor(detail, parent, !!locked);
    selected.value = detail;
    const base = { ...detail, ...currentForm.initial };
    const initial = Object.fromEntries(
      currentForm.schema
        .filter(({ fieldName }) => fieldName in base)
        .map(({ fieldName }) => [fieldName, (base as FormValues)[fieldName]]),
    );
    formApi.setState({
      schema: currentForm.schema,
      commonConfig: { componentProps: { disabled: !!locked } },
    });
    drawerApi.setState({
      title: `${locked ? '查看' : row ? '编辑' : '新增'}${props.label}`,
      showConfirmButton: !locked,
    });
    drawerApi.open();
    await formApi.resetForm();
    await formApi.setValues(initial);
  } catch {
    // The request client reports errors before the drawer opens.
  } finally {
    loadingEditor = false;
  }
}

async function save() {
  if (saving) return;
  saving = true;
  drawerApi.lock();
  try {
    if (!(await formApi.validate()).valid) return;
    const values = await formApi.getValues();
    const form = Object.fromEntries(
      currentForm.schema.map(({ fieldName }) => [fieldName, values[fieldName]]),
    );
    let body: FormValues;
    try {
      body = currentForm.body?.(form) ?? form;
    } catch (error) {
      message.error(error instanceof Error ? error.message : '请检查表单');
      return;
    }
    if (selected.value) await props.api.update(selected.value, body);
    else await props.api.create(body);
    await drawerApi.close();
    message.success('保存成功');
    await refresh();
  } catch (error) {
    if (isConflict(error)) {
      await drawerApi.close();
      await refresh();
    }
  } finally {
    drawerApi.unlock();
    saving = false;
  }
}

function remove(row: R) {
  adminMutation(
    `删除${props.label}${props.rowLabel ? `「${props.rowLabel(row)}」` : ''}？`,
    () => props.api.remove(row),
    () => gridApi.reload(),
  );
}
</script>

<template>
  <div class="min-w-0">
    <Grid :table-title="label">
      <template #toolbar-tools>
        <Space wrap>
          <slot name="toolbar" :refresh="refresh" />
          <Button
            v-if="rootCreate !== false && can('Create')"
            type="primary"
            @click="openEditor()"
          >
            新增
          </Button>
        </Space>
      </template>
      <template #actions="{ row }">
        <Space :size="2" wrap>
          <slot name="actions" :row="row" :refresh="refresh" />
          <Button
            v-if="can('View')"
            size="small"
            type="link"
            @click="openEditor(row, undefined, true)"
            >查看</Button
          >
          <Button
            v-if="can('Update') && editable?.(row) !== false"
            size="small"
            type="link"
            @click="openEditor(row)"
            >编辑</Button
          >
          <Button
            v-if="child?.(row) && can('Create')"
            size="small"
            type="link"
            @click="openEditor(undefined, row)"
            >添加下级</Button
          >
          <Button
            v-if="can('Delete') && removable?.(row) !== false"
            danger
            size="small"
            type="link"
            @click="remove(row)"
            >删除</Button
          >
        </Space>
      </template>
    </Grid>
    <Drawer><Form /></Drawer>
  </div>
</template>
