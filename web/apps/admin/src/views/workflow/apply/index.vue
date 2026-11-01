<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import DynamicForm from '@/components/DynamicForm.vue'
import { applyWorkflow, listAllFlowForms, parseFlowFormSchema } from '@/api/workflow'
import type { FlowFormDef } from '@/api/workflow'

const { t } = useI18n()

const formsLoading = ref(false)
const forms = ref<FlowFormDef[]>([])
const selectedKey = ref('')
const formData = ref<Record<string, unknown>>({})
const submitting = ref(false)
const dynamicFormRef = ref<InstanceType<typeof DynamicForm>>()

const currentForm = computed<FlowFormDef | undefined>(() =>
  forms.value.find((item) => item.formKey === selectedKey.value),
)

const fields = computed(() =>
  currentForm.value ? parseFlowFormSchema(currentForm.value.schemaJson) : [],
)

async function fetchForms(): Promise<void> {
  formsLoading.value = true
  try {
    forms.value = await listAllFlowForms()
    const initial = forms.value.find((item) => item.formKey === 'leave') ?? forms.value[0]
    selectedKey.value = initial?.formKey ?? ''
  } finally {
    formsLoading.value = false
  }
}

watch(selectedKey, () => {
  formData.value = {}
})

async function handleSubmit(): Promise<void> {
  const form = currentForm.value
  if (!form) {
    return
  }
  const valid = (await dynamicFormRef.value?.validate()) ?? false
  if (!valid) {
    return
  }
  const data: Record<string, string | number> = {}
  for (const field of fields.value) {
    const value = formData.value[field.key]
    if (typeof value === 'string' && value !== '') {
      data[field.key] = value
    } else if (typeof value === 'number' && Number.isFinite(value)) {
      data[field.key] = value
    }
  }
  submitting.value = true
  try {
    await applyWorkflow({ formKey: form.formKey, data })
    ElMessage.success(t('flow.applySuccess'))
    formData.value = {}
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void fetchForms()
})
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span class="toolbar-label">{{ t('flow.selectForm') }}</span>
      <el-select
        v-model="selectedKey"
        class="form-select"
        :loading="formsLoading"
        :placeholder="t('flow.selectFormPlaceholder')"
      >
        <el-option
          v-for="form in forms"
          :key="form.formKey"
          :label="form.name"
          :value="form.formKey"
        />
      </el-select>
    </div>

    <DynamicForm
      v-if="currentForm && fields.length > 0"
      ref="dynamicFormRef"
      v-model="formData"
      :schema-json="currentForm.schemaJson"
    />
    <el-empty v-else-if="currentForm" :description="t('flow.noFormFields')" />

    <div class="actions">
      <el-button
        type="primary"
        :loading="submitting"
        :disabled="!currentForm || fields.length === 0"
        @click="handleSubmit"
      >
        {{ t('flow.submitApply') }}
      </el-button>
    </div>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.toolbar-label {
  color: var(--el-text-color-regular);
}
.form-select {
  width: 240px;
}
.actions {
  margin-top: 14px;
}
</style>
