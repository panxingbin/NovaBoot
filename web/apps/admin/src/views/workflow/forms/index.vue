<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Delete, Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  createFlowForm,
  deleteFlowForm,
  pageFlowForms,
  parseFlowFormSchema,
  updateFlowForm,
} from '@/api/workflow'
import type { FlowFieldType, FlowFormDef, FlowFormField, FlowFormSaveBody } from '@/api/workflow'
import { formatDateTime } from '@/utils/format'

const { t } = useI18n()

const loading = ref(false)
const total = ref(0)
const records = ref<FlowFormDef[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const keyword = query.keyword.trim()
    const data = await pageFlowForms({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: keyword || undefined,
    })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch(): void {
  query.pageNum = 1
  void fetchList()
}

function handleReset(): void {
  query.keyword = ''
  handleSearch()
}

function handleSizeChange(): void {
  query.pageNum = 1
  void fetchList()
}

// ---------- 字段編輯器 ----------

interface FieldRow {
  key: string
  label: string
  type: FlowFieldType
  required: boolean
  /** select 類型的選項文本，逗號分隔 */
  optionsText: string
  min: number | undefined
  max: number | undefined
}

const typeOptions: FlowFieldType[] = ['input', 'textarea', 'number', 'date', 'select']

function typeLabel(type: FlowFieldType): string {
  switch (type) {
    case 'input':
      return t('flow.typeInput')
    case 'textarea':
      return t('flow.typeTextarea')
    case 'number':
      return t('flow.typeNumber')
    case 'date':
      return t('flow.typeDate')
    case 'select':
      return t('flow.typeSelect')
  }
}

function emptyRow(): FieldRow {
  return { key: '', label: '', type: 'input', required: false, optionsText: '', min: undefined, max: undefined }
}

function rowsFromSchema(schemaJson: string): FieldRow[] {
  return parseFlowFormSchema(schemaJson).map((field) => ({
    key: field.key,
    label: field.label,
    type: field.type,
    required: field.required === true,
    optionsText: (field.options ?? []).join(','),
    min: field.min,
    max: field.max,
  }))
}

function serializeRows(rows: FieldRow[]): string {
  const fields: FlowFormField[] = []
  for (const row of rows) {
    const key = row.key.trim()
    if (!key) {
      continue
    }
    const field: FlowFormField = { key, label: row.label.trim() || key, type: row.type }
    if (row.required) {
      field.required = true
    }
    if (row.type === 'select') {
      const options = row.optionsText
        .split(/[,，]/)
        .map((item) => item.trim())
        .filter(Boolean)
      if (options.length > 0) {
        field.options = options
      }
    }
    if (row.type === 'number') {
      if (typeof row.min === 'number' && Number.isFinite(row.min)) {
        field.min = row.min
      }
      if (typeof row.max === 'number' && Number.isFinite(row.max)) {
        field.max = row.max
      }
    }
    fields.push(field)
  }
  return JSON.stringify(fields)
}

// ---------- 新增 / 修改 ----------

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(0)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ formKey: '', name: '', remark: '' })
const fieldRows = ref<FieldRow[]>([])

const rules = computed<FormRules>(() => ({
  formKey: [{ required: true, message: () => t('flow.formKeyRequired'), trigger: 'blur' }],
  name: [{ required: true, message: () => t('flow.nameRequired'), trigger: 'blur' }],
}))

function openCreate(): void {
  isEdit.value = false
  editingId.value = 0
  form.formKey = ''
  form.name = ''
  form.remark = ''
  fieldRows.value = [emptyRow()]
  dialogVisible.value = true
}

function openEdit(row: FlowFormDef): void {
  isEdit.value = true
  editingId.value = row.id
  form.formKey = row.formKey
  form.name = row.name
  form.remark = row.remark ?? ''
  fieldRows.value = rowsFromSchema(row.schemaJson)
  if (fieldRows.value.length === 0) {
    fieldRows.value = [emptyRow()]
  }
  dialogVisible.value = true
}

function addRow(): void {
  fieldRows.value.push(emptyRow())
}

function removeRow(index: number): void {
  fieldRows.value.splice(index, 1)
}

async function handleSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  const rows = fieldRows.value
  if (rows.some((row) => row.key.trim() === '' || row.label.trim() === '')) {
    ElMessage.error(t('flow.fieldRowInvalid'))
    return
  }
  const keys = rows.map((row) => row.key.trim())
  const duplicate = keys.find((key, index) => keys.indexOf(key) !== index)
  if (duplicate) {
    ElMessage.error(t('flow.fieldKeyDuplicate', { key: duplicate }))
    return
  }
  const body: FlowFormSaveBody = {
    formKey: form.formKey.trim(),
    name: form.name.trim(),
    schemaJson: serializeRows(rows),
    remark: form.remark.trim() || undefined,
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateFlowForm(editingId.value, body)
      ElMessage.success(t('flow.updateSuccess'))
    } else {
      await createFlowForm(body)
      ElMessage.success(t('flow.createSuccess'))
    }
    dialogVisible.value = false
    void fetchList()
  } finally {
    saving.value = false
  }
}

// ---------- 刪除 ----------

async function handleDelete(row: FlowFormDef): Promise<void> {
  try {
    await ElMessageBox.confirm(t('flow.deleteConfirm', { name: row.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteFlowForm(row.id)
  ElMessage.success(t('common.deleteSuccess'))
  if (records.value.length === 1 && query.pageNum > 1) {
    query.pageNum -= 1
  }
  void fetchList()
}

onMounted(() => {
  void fetchList()
})
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        class="search-input"
        :placeholder="t('flow.searchPlaceholder')"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">{{ t('common.search') }}</el-button>
      <el-button :icon="Refresh" @click="handleReset">{{ t('common.reset') }}</el-button>
      <el-button v-permission="'workflow:form:add'" type="primary" :icon="Plus" @click="openCreate">
        {{ t('flow.addForm') }}
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="formKey" :label="t('flow.formKey')" min-width="140" show-overflow-tooltip />
      <el-table-column prop="name" :label="t('flow.name')" min-width="140" show-overflow-tooltip />
      <el-table-column :label="t('common.remark')" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ (row as FlowFormDef).remark || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('common.createdAt')" width="170">
        <template #default="{ row }">{{ formatDateTime((row as FlowFormDef).createdAt) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="140" fixed="right">
        <template #default="{ row }">
          <el-button
            v-permission="'workflow:form:edit'"
            link
            type="primary"
            @click="openEdit(row as FlowFormDef)"
          >
            {{ t('common.edit') }}
          </el-button>
          <el-button
            v-permission="'workflow:form:delete'"
            link
            type="danger"
            @click="handleDelete(row as FlowFormDef)"
          >
            {{ t('common.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="fetchList"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? t('flow.editForm') : t('flow.addForm')"
      width="880px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="t('flow.formKey')" prop="formKey">
          <el-input v-model="form.formKey" :disabled="isEdit" :placeholder="t('flow.formKeyPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('flow.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('flow.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('common.remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" :placeholder="t('flow.remarkPlaceholder')" />
        </el-form-item>
      </el-form>

      <div class="field-editor">
        <div class="field-editor-header">
          <span>{{ t('flow.fields') }}</span>
          <el-button type="primary" link :icon="Plus" @click="addRow">
            {{ t('flow.addField') }}
          </el-button>
        </div>
        <div class="field-columns">
          <span class="col-key">{{ t('flow.fieldKey') }}</span>
          <span class="col-label">{{ t('flow.fieldLabel') }}</span>
          <span class="col-type">{{ t('flow.fieldType') }}</span>
          <span class="col-required">{{ t('flow.requiredLabel') }}</span>
          <span class="col-extra">{{ t('flow.fieldOptions') }} / {{ t('flow.fieldMin') }}·{{ t('flow.fieldMax') }}</span>
          <span class="col-action" />
        </div>
        <div v-for="(row, index) in fieldRows" :key="index" class="field-row">
          <el-input v-model="row.key" class="col-key" :placeholder="t('flow.fieldKey')" />
          <el-input v-model="row.label" class="col-label" :placeholder="t('flow.fieldLabel')" />
          <el-select v-model="row.type" class="col-type">
            <el-option v-for="type in typeOptions" :key="type" :label="typeLabel(type)" :value="type" />
          </el-select>
          <div class="col-required row-required">
            <el-switch v-model="row.required" />
          </div>
          <div class="col-extra">
            <el-input
              v-if="row.type === 'select'"
              v-model="row.optionsText"
              :placeholder="t('flow.optionsPlaceholder')"
            />
            <template v-else-if="row.type === 'number'">
              <el-input-number v-model="row.min" class="row-minmax" :controls="false" :placeholder="t('flow.fieldMin')" />
              <el-input-number v-model="row.max" class="row-minmax" :controls="false" :placeholder="t('flow.fieldMax')" />
            </template>
            <span v-else>-</span>
          </div>
          <div class="col-action">
            <el-button link type="danger" :icon="Delete" @click="removeRow(index)" />
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.search-input {
  width: 240px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.field-editor {
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 12px;
}
.field-editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  color: var(--el-text-color-primary);
  font-weight: 600;
}
.field-columns,
.field-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.field-columns {
  margin-bottom: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.field-row {
  margin-bottom: 8px;
}
.col-key {
  width: 150px;
}
.col-label {
  width: 150px;
}
.col-type {
  width: 120px;
}
.col-required {
  width: 56px;
  text-align: center;
}
.col-extra {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.row-minmax {
  flex: 1;
  min-width: 0;
}
.row-required {
  justify-content: center;
}
.col-action {
  width: 40px;
  text-align: right;
}
</style>
