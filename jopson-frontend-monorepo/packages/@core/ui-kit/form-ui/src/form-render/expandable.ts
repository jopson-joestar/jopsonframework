import type { ComputedRef } from 'vue';

import type { FormRenderProps } from '../types';

import { nextTick, shallowRef, useTemplateRef, watch } from 'vue';

import {
  breakpointsTailwind,
  useBreakpoints,
  useElementVisibility,
  useMutationObserver,
} from '@vueuse/core';

export function useExpandable(
  props: FormRenderProps,
  formItemClasses: ComputedRef<(string | undefined)[]>,
) {
  const wrapperRef = useTemplateRef<HTMLElement>('wrapperRef');
  const visible = useElementVisibility(wrapperRef);
  const breakpoint = useBreakpoints(breakpointsTailwind).active();
  const hiddenFields = shallowRef(new Set<string>());

  async function measure() {
    // Use the expanded layout even when the user is currently viewing one row.
    hiddenFields.value = new Set();
    await nextTick();
    const container = wrapperRef.value;
    if (!props.showCollapseButton || !container?.offsetWidth) return;

    const style = getComputedStyle(container);
    const heights = style.gridTemplateRows.split(' ').map(Number.parseFloat);
    if (!Number.isFinite(heights[0])) return;
    const rows = Math.max(1, props.collapsedRows ?? 1);
    const gap = Number.parseFloat(style.rowGap) || 0;
    const top = container.getBoundingClientRect().top;
    const lastRowTop =
      top + heights.slice(0, rows - 1).reduce((sum, h) => sum + h + gap, 0);
    const lastRowBottom = lastRowTop + (heights[rows - 1] ?? 0);
    const actions = container
      .querySelector(':scope > [data-action-layout="rowEnd"]')
      ?.getBoundingClientRect();
    const fields = [
      ...container.querySelectorAll<HTMLElement>(':scope > [data-form-field]'),
    ].filter((field) => field.getClientRects().length > 0);

    const firstHidden = fields.findIndex((field) => {
      const rect = field.getBoundingClientRect();
      if (rect.top >= lastRowBottom - 0.5) return true;
      // A spanning field can occupy the final track as well.
      return (
        actions &&
        rect.top >= lastRowTop - 0.5 &&
        rect.right > actions.left + 0.5 &&
        rect.left < actions.right - 0.5
      );
    });
    if (firstHidden >= 0) {
      // In a single column, leave one field visible and wrap the actions below it.
      hiddenFields.value = new Set(
        fields
          .slice(Math.max(1, firstHidden))
          .map((field) => field.dataset.formField!),
      );
    }
  }

  watch(
    [
      wrapperRef,
      visible,
      breakpoint,
      () => props.showCollapseButton,
      () => props.wrapperClass,
      () => props.collapsedRows,
      () => props.layout,
      formItemClasses,
      () => props.schema?.map((field) => [field.fieldName, field.hide]),
    ],
    measure,
    { flush: 'post' },
  );

  // Conditional fields can mount, unmount or toggle v-show independently of the schema.
  useMutationObserver(
    () =>
      props.showCollapseButton &&
      props.schema?.some((field) => field.dependencies)
        ? wrapperRef.value
        : null,
    (records) => {
      if (
        records.some(
          ({ target }) =>
            target === wrapperRef.value ||
            (target instanceof HTMLElement &&
              target.parentElement === wrapperRef.value),
        )
      ) {
        void measure();
      }
    },
    { attributeFilter: ['style'], childList: true, subtree: true },
  );
  return hiddenFields;
}
