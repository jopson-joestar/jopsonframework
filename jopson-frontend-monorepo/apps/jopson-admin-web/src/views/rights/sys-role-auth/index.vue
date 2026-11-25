<script setup lang="ts">
import type { ResourceRow, RoleResourceTree, RoleRow } from '#/api/rights';

import { computed, onMounted, ref, shallowRef, watch } from 'vue';
import { useRoute } from 'vue-router';

import { Page } from '@vben/common-ui';
import { Button, Empty, message, Spin, Tree, TreeSelect } from 'antdv-next';

import { getRoleResources, roleApi, saveRoleResources } from '#/api/rights';
import { treeOptions } from '#/views/_admin/tree-options';
import { isConflict, useAdminActions } from '#/views/_admin/use-admin-actions';

const { can } = useAdminActions('SysRoleAuth');
const route = useRoute();
const roles = shallowRef<RoleRow[]>([]);
const activeRole = shallowRef<RoleRow>();
const selectedId = ref<string>();
const details = shallowRef<RoleResourceTree>();
const checked = ref<string[]>([]);
const loading = ref(false);
const saving = ref(false);
const roleChoices = computed(() =>
  treeOptions(
    roles.value,
    (row) => row.sysRoleId,
    (row) => row.sysRoleName,
  ),
);
const readOnly = computed(() => !can('AuthRes') || !!activeRole.value?.sysData);
const resourceNodes = computed(() => {
  const assignable = new Set(details.value?.assignableKeys ?? []);
  function makeTree(rows: ResourceRow[]): {
    key: string;
    title: string;
    disableCheckbox: boolean;
    children: ReturnType<typeof makeTree>;
  }[] {
    return rows.map((row) => ({
      key: row.sysResId,
      title:
        String(row.routeMeta?.title ?? row.routeName) +
        (row.permission ? ` · ${row.permission}` : ''),
      disableCheckbox: !assignable.has(row.sysResId),
      children: makeTree(row.children),
    }));
  }
  return makeTree(details.value?.rows ?? []);
});
let requestVersion = 0;

function findRole(
  id: string | undefined,
  rows: RoleRow[],
): RoleRow | undefined {
  for (const row of rows) {
    if (row.sysRoleId === id) return row;
    const child = findRole(id, row.children);
    if (child) return child;
  }
}

async function load(id = selectedId.value) {
  const version = ++requestVersion;
  details.value = undefined;
  checked.value = [];
  activeRole.value = findRole(id, roles.value);
  if (!id || !activeRole.value) {
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    const result = await getRoleResources(id);
    if (version === requestVersion) {
      details.value = result;
      checked.value = [...result.checkedKeys];
    }
  } finally {
    if (version === requestVersion) loading.value = false;
  }
}

function changeChecked(
  _keys: unknown,
  event: { checked: boolean; node: { key: number | string } },
) {
  if (readOnly.value || !details.value) return;
  const key = String(event.node.key);
  const parents = new Map<string, string[]>();
  function visit(rows: ResourceRow[], ancestors: string[]) {
    for (const row of rows) {
      parents.set(row.sysResId, ancestors);
      visit(row.children, [...ancestors, row.sysResId]);
    }
  }
  visit(details.value.rows, []);
  const selected = new Set(checked.value);
  if (event.checked) {
    const assignable = new Set(details.value.assignableKeys);
    const required = [key, ...(parents.get(key) ?? [])];
    if (!required.every((id) => assignable.has(id))) {
      message.warning('上级资源超出可授予范围');
      return;
    }
    required.forEach((id) => selected.add(id));
  } else {
    selected.delete(key);
    for (const [id, ancestors] of parents)
      if (ancestors.includes(key)) selected.delete(id);
  }
  checked.value = [...selected];
}

async function save() {
  if (!selectedId.value || !details.value || saving.value || readOnly.value)
    return;
  saving.value = true;
  try {
    await saveRoleResources(
      selectedId.value,
      details.value.version,
      checked.value,
    );
    message.success('授权已保存');
    await load();
  } catch (error) {
    if (isConflict(error)) await load();
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  roles.value = await roleApi.tree();
  const id =
    typeof route.query.role === 'string' ? route.query.role : undefined;
  if (findRole(id, roles.value)) selectedId.value = id;
});
watch(selectedId, (id) => {
  void load(id).catch(() => {});
});
</script>

<template>
  <Page title="角色授权">
    <section class="rounded-md border bg-background p-4">
      <div class="mb-4 flex flex-wrap items-center gap-3">
        <label for="role-resource-selector">角色</label>
        <TreeSelect
          id="role-resource-selector"
          v-model:value="selectedId"
          :tree-data="roleChoices"
          :disabled="saving"
          class="w-full sm:w-72"
          placeholder="请选择角色"
          show-search
          tree-node-filter-prop="label"
        />
        <Button
          v-if="details && !readOnly"
          :loading="saving"
          type="primary"
          @click="save"
          >保存授权</Button
        >
        <span v-if="activeRole?.sysData" class="text-sm text-muted-foreground"
          >内置管理员角色仅供查看</span
        >
      </div>
      <Spin :spinning="loading">
        <Tree
          v-if="details && resourceNodes.length"
          :key="selectedId"
          :tree-data="resourceNodes"
          :checked-keys="{ checked, halfChecked: [] }"
          :disabled="readOnly || saving"
          checkable
          check-strictly
          default-expand-all
          @check="changeChecked"
        />
        <Empty
          v-else
          :description="selectedId ? '暂无可授予资源' : '选择角色查看资源授权'"
        />
      </Spin>
    </section>
  </Page>
</template>
