import type { VxeTableGridOptions } from '@vben/plugins/vxe-table';

import type { ComponentPropsMap, ComponentType } from './component';

import { h } from 'vue';

import {
  setupVbenVxeTable,
  useVbenVxeGrid as useGrid,
} from '@vben/plugins/vxe-table';

import { Button, Image } from 'antdv-next';

import { useVbenForm } from './form';

setupVbenVxeTable({
  configVxeTable: (vxeUI) => {
    vxeUI.setConfig({
      grid: {
        align: 'left',
        headerAlign: 'left',
        border: false,
        columnConfig: {
          resizable: true,
        },
        minHeight: 180,
        formConfig: {
          // 全局禁用vxe-table的表单配置，使用formOptions
          enabled: false,
        },
        proxyConfig: {
          autoLoad: true,
          response: {
            result: 'items',
            total: 'total',
            list: 'items',
          },
          showActiveMsg: true,
          showResponseMsg: false,
        },
        round: true,
        showOverflow: true,
        size: 'small',
      } as VxeTableGridOptions,
    });

    // 表格配置项可以用 cellRender: { name: 'CellImage' },
    vxeUI.renderer.add('CellImage', {
      renderTableDefault(renderOpts, params) {
        const { props } = renderOpts;
        const { column, row } = params;
        return h(Image, { src: row[column.field], ...props });
      },
    });

    // 表格配置项可以用 cellRender: { name: 'CellLink' },
    vxeUI.renderer.add('CellLink', {
      renderTableDefault(renderOpts) {
        const { props } = renderOpts;
        return h(
          Button,
          { size: 'small', type: 'link' },
          { default: () => props?.text },
        );
      },
    });

    // 这里可以自行扩展 vxe-table 的全局配置，比如自定义格式化
    // vxeUI.formats.add
  },
  useVbenForm,
});

interface SizedColumn {
  children?: (SizedColumn | undefined)[];
  minWidth?: number | string;
  type?: null | string;
  width?: number | string;
}

function withColumnWidths<C extends SizedColumn | undefined>(
  columns: C[],
): C[] {
  return columns.map((column) => {
    if (!column) return column;
    if (column.children) {
      return { ...column, children: withColumnWidths(column.children) };
    }
    if (column.width == null && column.minWidth == null) {
      if (column.type === 'checkbox' || column.type === 'radio') {
        return { ...column, width: 48 };
      }
      if (column.type === 'seq') return { ...column, width: 56 };
    }
    return column;
  });
}

export function useVbenVxeGrid<T extends Record<string, any>>(
  options: Parameters<typeof useGrid<T, ComponentType, ComponentPropsMap>>[0],
) {
  const { formOptions, gridOptions } = options;
  return useGrid<T, ComponentType, ComponentPropsMap>({
    ...options,
    ...(formOptions && {
      formOptions: {
        actionLayout: 'rowEnd',
        collapsed: true,
        collapsedRows: 1,
        wrapperClass:
          'grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4',
        ...formOptions,
      },
    }),
    ...(gridOptions?.columns && {
      gridOptions: {
        ...gridOptions,
        columns: withColumnWidths(gridOptions.columns),
      },
    }),
  });
}

export type * from '@vben/plugins/vxe-table';
