import type { Page } from 'playwright/test';
import { expect, test } from 'playwright/test';

const fixture = '/e2e/grid-layout/index.html';
const field = (page: Page, name: string) =>
  page.locator(`[data-form-field="${name}"]`);
const actions = (page: Page) => page.locator('[data-action-layout="rowEnd"]');
const toggle = (page: Page) =>
  actions(page).getByRole('button', { name: /展开|收起/ });

async function openList(page: Page, mode = '') {
  const queries: Array<Record<string, string>> = [];
  await page.route('**/__grid-test/records?**', async (route) => {
    const query = Object.fromEntries(
      new URL(route.request().url()).searchParams,
    );
    queries.push(query);
    await route.fulfill({
      json: {
        code: 0,
        message: '',
        data: {
          currentPage: Number(query.currentPage),
          pageSize: Number(query.pageSize),
          items: [
            {
              number: `单据-${query.currentPage}-${query.keyword}`,
              amount: 168,
            },
          ],
          total: 44,
        },
      },
    });
  });
  await page.goto(`${fixture}?mode=${mode}`, { waitUntil: 'domcontentloaded' });
  await expect(page.getByText('单据-1-', { exact: true })).toBeVisible();
  return queries;
}

async function expectCollapsedRow(page: Page, columns: number) {
  const visible = page.locator('[data-form-field]:visible');
  await expect(visible).toHaveCount(Math.max(1, columns - 1));
  await expect(toggle(page)).toHaveAttribute('aria-expanded', 'false');
  const grid = field(page, 'keyword').locator('..');
  await expect
    .poll(() =>
      grid.evaluate(
        (el) => getComputedStyle(el).gridTemplateColumns.split(' ').length,
      ),
    )
    .toBe(columns);
  const lastField = await visible.last().boundingBox();
  const buttons = await actions(page).boundingBox();
  if (!lastField || !buttons) throw new Error('查询区未显示');
  if (columns === 1) {
    expect(buttons.y).toBeGreaterThan(lastField.y);
  } else {
    expect(Math.abs(buttons.y - lastField.y)).toBeLessThan(5);
    expect(buttons.x).toBeGreaterThanOrEqual(lastField.x + lastField.width - 1);
  }
  const fits = await grid.evaluate(
    (el) => el.scrollWidth <= el.clientWidth + 1,
  );
  expect(fits).toBe(true);
}

test('查询区随视口和侧栏切换，保留已输入的值', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 });
  await openList(page);
  const keyword = field(page, 'keyword').locator('input');
  await keyword.fill('待审核');
  for (const [width, columns] of [
    [390, 1],
    [820, 2],
    [1100, 3],
    [1280, 4],
    [1440, 4],
    [1920, 4],
  ] as const) {
    await page.setViewportSize({ width, height: 900 });
    await expectCollapsedRow(page, columns);
    if (columns === 4) {
      const grid = field(page, 'keyword').locator('..');
      const before = await grid.evaluate((el) => el.clientWidth);
      await page.getByRole('button', { name: '切换测试侧栏' }).click();
      await expect
        .poll(() => grid.evaluate((el) => el.clientWidth))
        .not.toBe(before);
      await expectCollapsedRow(page, columns);
    }
    await expect(keyword).toHaveValue('待审核');
  }
  await toggle(page).click();
  await expect(field(page, 'period')).toBeVisible();
  const period = await field(page, 'period').boundingBox();
  const single = await field(page, 'keyword').boundingBox();
  expect(period!.width).toBeGreaterThan(single!.width * 1.9);
  const status = field(page, 'status').locator('input');
  await status.fill('已提交');
  await toggle(page).click();
  await expect(field(page, 'period')).toBeHidden();
  await expect(status).toBeHidden();
  await page.setViewportSize({ width: 820, height: 900 });
  await expectCollapsedRow(page, 2);
  await toggle(page).click();
  await expect(status).toHaveValue('已提交');
  await expect(keyword).toHaveValue('待审核');
});

test('恰好三项、窄容器和初始展开时尊重页面配置', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 });
  await openList(page, 'three');
  await expect(page.locator('[data-form-field]:visible')).toHaveCount(3);
  await expect(toggle(page)).toHaveCount(0);
  await page.goto(`${fixture}?mode=narrow`);
  await expectCollapsedRow(page, 2);
  await page.goto(`${fixture}?mode=expanded`);
  await expect(page.locator('[data-form-field]:visible')).toHaveCount(5);
  await expect(toggle(page)).toHaveAttribute('aria-expanded', 'true');
});

test('跨列字段按实际宽度折叠，无查询区时只渲染表格', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 });
  await openList(page, 'span');
  await expect(page.locator('[data-form-field]:visible')).toHaveCount(2);
  await toggle(page).focus();
  await page.keyboard.press('Enter');
  await expect(field(page, 'period')).toBeVisible();
  await page.goto(`${fixture}?mode=bare`, { waitUntil: 'domcontentloaded' });
  await expect(page.getByText('单据-1-', { exact: true })).toBeVisible();
  await expect(actions(page)).toHaveCount(0);
});

test('表格布局、查询、翻页和重置', async ({ page }) => {
  const queries = await openList(page);
  const heads = page.locator('.vxe-header--column');
  const cells = page.locator('.vxe-body--column');
  await expect(heads.nth(2)).toHaveCSS('text-align', 'left');
  await expect(cells.nth(2)).toHaveCSS('text-align', 'left');
  await expect(heads.nth(3)).toHaveCSS('text-align', 'right');
  await expect(cells.nth(3)).toHaveCSS('text-align', 'right');
  expect((await heads.nth(0).boundingBox())!.width).toBeCloseTo(48, 1);
  expect((await heads.nth(1).boundingBox())!.width).toBeCloseTo(56, 1);
  await expect(
    cells.last().getByRole('button', { name: '查看单据' }),
  ).toBeVisible();
  await expect(page.locator('.vxe-pager--total')).toContainText('44');
  await field(page, 'keyword').locator('input').fill('已审');
  await actions(page)
    .getByRole('button', { name: /搜\s*索/ })
    .click();
  await expect(page.getByText('单据-1-已审', { exact: true })).toBeVisible();
  await page.getByTitle('下一页', { exact: true }).click();
  await expect(page.getByText('单据-2-已审', { exact: true })).toBeVisible();
  await actions(page)
    .getByRole('button', { name: /重\s*置/ })
    .click();
  await expect(page.getByText('单据-1-', { exact: true })).toBeVisible();
  expect(queries.map((q) => [q.currentPage, q.pageSize, q.keyword])).toEqual([
    ['1', '20', ''],
    ['1', '20', '已审'],
    ['2', '20', '已审'],
    ['1', '20', ''],
  ]);
});

test('正式入口仍显示登录表单', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveURL(/\/auth\/login/);
  await expect(page.locator('input[type=password]')).toBeVisible();
  await expect(
    page.getByRole('button', { name: 'login', exact: true }),
  ).toBeVisible();
});
