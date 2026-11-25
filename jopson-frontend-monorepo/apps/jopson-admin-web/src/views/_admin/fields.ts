import type { FormValues } from '#/api/admin-records';

import type { VbenFormSchema } from '#/adapter/form';

import { z } from '#/adapter/form';

type Option = { label: string; value: boolean | number | string };

export function input(
  fieldName: string,
  label: string,
  maxLength?: number,
  required = false,
): VbenFormSchema {
  return {
    component: 'Input',
    fieldName,
    label,
    componentProps: { maxLength },
    rules: required
      ? z
          .string()
          .trim()
          .min(1, `请输入${label}`)
          .max(maxLength ?? 500)
      : undefined,
  };
}

export function textarea(
  fieldName: string,
  label: string,
  maxLength?: number,
): VbenFormSchema {
  return {
    component: 'Textarea',
    fieldName,
    label,
    componentProps: { maxLength, rows: 3 },
    formItemClass: 'md:col-span-2',
  };
}

export function select(
  fieldName: string,
  label: string,
  options: Option[],
  required = false,
): VbenFormSchema {
  return {
    component: 'Select',
    fieldName,
    label,
    componentProps: { allowClear: !required, options },
    rules: required ? 'selectRequired' : undefined,
  };
}

export function numeric(
  fieldName: string,
  label: string,
  min?: number,
  max?: number,
): VbenFormSchema {
  return {
    component: 'InputNumber',
    fieldName,
    label,
    componentProps: { min, max, precision: 0, style: { width: '100%' } },
    rules:
      min === undefined
        ? undefined
        : z
            .number()
            .int()
            .min(min)
            .max(max ?? Number.MAX_SAFE_INTEGER),
  };
}

export function toggle(
  fieldName: string,
  label: string,
  defaultValue = false,
  disabled = false,
): VbenFormSchema {
  return {
    component: 'Switch',
    fieldName,
    label,
    defaultValue,
    componentProps: { disabled },
  };
}

export function dateRange(): VbenFormSchema[] {
  return [
    {
      component: 'DatePicker',
      fieldName: 'createTimeBegin',
      label: '创建时间起',
      componentProps: { showTime: true, valueFormat: 'YYYY-MM-DD HH:mm:ss' },
    },
    {
      component: 'DatePicker',
      fieldName: 'createTimeEnd',
      label: '创建时间止',
      componentProps: { showTime: true, valueFormat: 'YYYY-MM-DD HH:mm:ss' },
    },
  ];
}

export function parseJsonValue(value: unknown, label: string): unknown {
  if (typeof value !== 'string' || !value.trim()) return null;
  try {
    return JSON.parse(value);
  } catch {
    throw new Error(`${label}须为有效的 JSON`);
  }
}

export interface EditorForm {
  schema: VbenFormSchema[];
  initial?: FormValues;
  body?: (values: FormValues) => FormValues;
}
