<script setup lang="ts">
import type { GenericObject } from 'vee-validate';
import type { ZodTypeAny } from 'zod';

import type {
  FormCommonConfig,
  FormRenderProps,
  FormSchema,
  FormShape,
} from '../types';

import { computed } from 'vue';

import { Form } from '@vben-core/shadcn-ui';
import {
  cn,
  isFunction,
  isString,
  mergeWithArrayOverride,
} from '@vben-core/shared/utils';

import { provideFormRenderProps } from './context';
import { useExpandable } from './expandable';
import FormField from './form-field.vue';
import { getBaseRules, getDefaultValueInZodStack } from './helper';

interface Props extends FormRenderProps {}

const props = withDefaults(
  defineProps<Props & { globalCommonConfig?: FormCommonConfig }>(),
  {
    collapsedRows: 1,
    commonConfig: () => ({}),
    globalCommonConfig: () => ({}),
    showCollapseButton: false,
    wrapperClass: 'grid-cols-1 sm:grid-cols-2 md:grid-cols-3',
  },
);

const emits = defineEmits<{
  submit: [event: any];
}>();

const wrapperClass = computed(() => {
  const cls = ['flex'];
  if (props.layout === 'inline') {
    cls.push('flex-wrap gap-x-2');
  } else {
    cls.push(props.compact ? 'gap-x-2' : 'gap-x-4', 'flex-col grid');
  }
  return cn(...cls, props.wrapperClass);
});

provideFormRenderProps(props);

const formItemClasses = computed(() =>
  (props.schema ?? []).map(({ formItemClass }) => {
    if (!isFunction(formItemClass)) return formItemClass;
    try {
      return formItemClass();
    } catch (error) {
      console.error('Error calling formItemClass function:', error);
      return '';
    }
  }),
);
const hiddenFields = useExpandable(props, formItemClasses);

const shapes = computed(() => {
  const resultShapes: FormShape[] = [];
  props.schema?.forEach((schema) => {
    const { fieldName } = schema;
    const rules = schema.rules as ZodTypeAny;

    let typeName = '';
    if (rules && !isString(rules)) {
      typeName = rules._def.typeName;
    }

    const baseRules = getBaseRules(rules) as ZodTypeAny;

    resultShapes.push({
      default: getDefaultValueInZodStack(rules),
      fieldName,
      required: !['ZodNullable', 'ZodOptional'].includes(typeName),
      rules: baseRules,
    });
  });
  return resultShapes;
});

const formComponent = computed(() => (props.form ? 'form' : Form));

const formComponentProps = computed(() => {
  return props.form
    ? {
        onSubmit: props.form.handleSubmit((val) => emits('submit', val)),
      }
    : {
        onSubmit: (val: GenericObject) => emits('submit', val),
      };
});

const computedSchema = computed(
  (): (Omit<FormSchema, 'formFieldProps'> & {
    commonComponentProps: Record<string, any>;
    formFieldProps: Record<string, any>;
  })[] => {
    const {
      colon = false,
      componentProps = {},
      controlClass = '',
      disabled,
      disabledOnChangeListener = true,
      disabledOnInputListener = true,
      emptyStateValue = undefined,
      formFieldProps = {},
      formItemClass = '',
      hideLabel = false,
      hideRequiredMark = false,
      labelClass = '',
      labelWidth = 100,
      modelPropName = '',
      wrapperClass = '',
    } = mergeWithArrayOverride(props.commonConfig, props.globalCommonConfig);
    return (props.schema || []).map((schema, index) => {
      const hidden = props.collapsed && hiddenFields.value.has(schema.fieldName);

      return {
        colon,
        disabled,
        disabledOnChangeListener,
        disabledOnInputListener,
        emptyStateValue,
        hideLabel,
        hideRequiredMark,
        labelWidth,
        modelPropName,
        wrapperClass,
        ...schema,
        commonComponentProps: componentProps,
        componentProps: schema.componentProps,
        controlClass: cn(controlClass, schema.controlClass),
        formFieldProps: {
          ...formFieldProps,
          ...schema.formFieldProps,
        },
        formItemClass: cn(
          'shrink-0',
          { hidden },
          formItemClass,
          formItemClasses.value[index],
        ),
        labelClass: cn(labelClass, schema.labelClass),
      };
    });
  },
);
</script>

<template>
  <component :is="formComponent" v-bind="formComponentProps">
    <div ref="wrapperRef" :class="wrapperClass">
      <template v-for="cSchema in computedSchema" :key="cSchema.fieldName">
        <!-- <div v-if="$slots[cSchema.fieldName]" :class="cSchema.formItemClass">
          <slot :definition="cSchema" :name="cSchema.fieldName"> </slot>
        </div> -->
        <FormField
          v-bind="cSchema"
          :data-form-field="cSchema.fieldName"
          :class="cSchema.formItemClass"
          :rules="cSchema.rules"
        >
          <template #default="slotProps">
            <slot v-bind="slotProps" :name="cSchema.fieldName"> </slot>
          </template>
        </FormField>
      </template>
      <slot :can-collapse="hiddenFields.size > 0" :shapes="shapes"></slot>
    </div>
  </component>
</template>
