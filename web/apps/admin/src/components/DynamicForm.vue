<script setup lang="ts">
import { computed, ref } from 'vue'
import type { FormInstance, FormItemRule } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { parseFlowFormSchema } from '@/api/workflow'
import type { FlowFormField } from '@/api/workflow'

const props = defineProps<{
  /** 表單 schema JSON 字符串 */
  schemaJson: string
  modelValue: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: Record<string, unknown>): void
}>()

const { t } = useI18n()

const formRef = ref<FormInstance>()

const fields = computed<FlowFormField[]>(() => parseFlowFormSchema(props.schemaJson))

function setValue(key: string, value: unknown): void {
  emit('update:modelValue', { ...props.modelValue, [key]: value })
}

function textValue(field: FlowFormField): string {
  const raw = props.modelValue[field.key]
  if (typeof raw === 'string') {
    return raw
  }
  if (typeof raw === 'number' && Number.isFinite(raw)) {
    return String(raw)
  }
  return ''
}

function numberValue(field: FlowFormField): number | undefined {
  const raw = props.modelValue[field.key]
  if (typeof raw === 'number' && Number.isFinite(raw)) {
    return raw
  }
  if (typeof raw === 'string' && raw.trim() !== '') {
    const parsed = Number(raw)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }
  return undefined
}

function dateValue(field: FlowFormField): string {
  const raw = props.modelValue[field.key]
  return typeof raw === 'string' ? raw : ''
}

function setNumberValue(field: FlowFormField, value: number | null | undefined): void {
  setValue(field.key, typeof value === 'number' && Number.isFinite(value) ? value : undefined)
}

function setDateValue(field: FlowFormField, value: unknown): void {
  setValue(field.key, typeof value === 'string' && value !== '' ? value : undefined)
}

function rulesFor(field: FlowFormField): FormItemRule[] {
  if (!field.required) {
    return []
  }
  return [
    {
      required: true,
      message: t('flow.requiredMessage', { label: field.label }),
      trigger: field.type === 'select' || field.type === 'date' ? 'change' : 'blur',
    },
  ]
}

async function validate(): Promise<boolean> {
  try {
    await formRef.value?.validate()
    return true
  } catch {
    return false
  }
}

defineExpose({ validate })
</script>

<template>
  <el-form ref="formRef" :model="modelValue" label-width="100px">
    <el-form-item
      v-for="field in fields"
      :key="field.key"
      :prop="field.key"
      :label="field.label"
      :rules="rulesFor(field)"
    >
      <el-input
        v-if="field.type === 'input'"
        :model-value="textValue(field)"
        @update:model-value="(value: string) => setValue(field.key, value)"
      />
      <el-input
        v-else-if="field.type === 'textarea'"
        type="textarea"
        :rows="3"
        :model-value="textValue(field)"
        @update:model-value="(value: string) => setValue(field.key, value)"
      />
      <el-input-number
        v-else-if="field.type === 'number'"
        :model-value="numberValue(field)"
        :min="field.min"
        :max="field.max"
        controls-position="right"
        @update:model-value="(value: number | undefined) => setNumberValue(field, value)"
      />
      <el-date-picker
        v-else-if="field.type === 'date'"
        type="date"
        value-format="YYYY-MM-DD"
        :model-value="dateValue(field)"
        @update:model-value="(value: unknown) => setDateValue(field, value)"
      />
      <el-select
        v-else
        :model-value="textValue(field)"
        @update:model-value="(value: unknown) => setValue(field.key, value)"
      >
        <el-option
          v-for="option in field.options ?? []"
          :key="option"
          :label="option"
          :value="option"
        />
      </el-select>
    </el-form-item>
  </el-form>
</template>
