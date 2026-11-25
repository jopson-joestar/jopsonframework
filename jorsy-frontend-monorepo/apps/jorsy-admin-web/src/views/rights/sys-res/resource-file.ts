import type { ResourceImportNode } from '#/api/rights';

import { z } from '#/adapter/form';

export const resourceTypes = [
  { label: '目录', value: 'FOLDER' },
  { label: '菜单', value: 'MENU' },
  { label: '按钮', value: 'BUTTON' },
  { label: '内嵌页', value: 'IFRAME' },
  { label: '外链', value: 'LINK' },
];

const nodeSchema: z.ZodType<ResourceImportNode> = z.lazy(() =>
  z.object({
    sortNo: z.number().int().nullish(),
    sysResType: z.enum(['FOLDER', 'MENU', 'BUTTON', 'IFRAME', 'LINK']),
    permission: z.string().max(100).nullish(),
    routeName: z
      .string()
      .max(100)
      .regex(/^[A-Z][A-Za-z0-9]*$/, '路由名须为 PascalCase'),
    routePath: z.string().max(500).nullish(),
    component: z.string().max(500).nullish(),
    redirect: z.string().max(500).nullish(),
    routeMeta: z
      .record(z.string(), z.unknown())
      .refine(
        (meta) => typeof meta.title === 'string' && !!meta.title.trim(),
        '资源标题不能为空',
      ),
    remark: z.string().max(500).nullish(),
    children: z.array(nodeSchema).optional(),
  }),
);

export function parseResourceFile(text: string) {
  const file: unknown = JSON.parse(text);
  const parsed = z
    .object({
      resources: z.array(nodeSchema).min(1, '资源列表不能为空'),
    })
    .safeParse(file);
  if (!parsed.success) {
    const issue = parsed.error.issues[0];
    if (!issue) throw new Error('无法读取资源列表');
    throw new Error(`${issue.path.join('.') || 'JSON'}：${issue.message}`);
  }

  const names = new Set<string>();
  const pending = [...parsed.data.resources];
  while (pending.length) {
    const node = pending.pop();
    if (!node) break;
    if (names.has(node.routeName))
      throw new Error(`路由名重复：${node.routeName}`);
    names.add(node.routeName);
    if (node.children) pending.push(...node.children);
  }
  return { resources: parsed.data.resources, count: names.size };
}
