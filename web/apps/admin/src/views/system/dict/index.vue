<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import {
  createDictData,
  createDictType,
  deleteDictData,
  deleteDictType,
  pageDictData,
  pageDictTypes,
  updateDictData,
  updateDictType,
} from '@/api/system'
import type { DictDataVO, DictTypeVO } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const { t } = useI18n()

// ---------- 左側：字典類型 ----------

const typeLoading = ref(false)
const typeTotal = ref(0)
const typeRecords = ref<DictTypeVO[]>([])
const typeQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const selectedType = ref<DictTypeVO | null>(null)

async function fetchTypes(): Promise<void> {
  typeLoading.value = true
  try {
    const keyword = typeQuery.keyword.trim()
    const data = await pageDictTypes({
      pageNum: typeQuery.pageNum,
      pageSize: typeQuery.pageSize,
      keyword: keyword || undefined,
    })
    typeRecords.value = data.records
    typeTotal.value = data.total
  } finally {
    typeLoading.value = false
  }
}

function handleTypeSearch(): void {
  typeQuery.pageNum = 1
  void fetchTypes()
}

function handleTypeReset(): void {
  typeQuery.keyword = ''
  handleTypeSearch()
}

function handleTypePageChange(): void {
  void fetchTypes()
}

function handleTypeSelect(row: DictTypeVO | null): void {
  selectedType.value = row
  dataQuery.pageNum = 1
  void fetchData()
}

const typeDialogVisible = ref(false)
const typeIsEdit = ref(false)
const typeEditingId = ref(0)
const typeFormRef = ref<FormInstance>()

const typeForm = reactive({ name: '', type: '', remark: '' })

const typeRules: FormRules = {
  name: [{ required: true, message: () => t('dict.typeNameRequired'), trigger: 'blur' }],
  type: [{ required: true, message: () => t('dict.typeCodeRequired'), trigger: 'blur' }],
}

function openTypeCreate(): void {
  typeIsEdit.value = false
  typeEditingId.value = 0
  Object.assign(typeForm, { name: '', type: '', remark: '' })
  typeDialogVisible.value = true
}

function openTypeEdit(row: DictTypeVO): void {
  typeIsEdit.value = true
  typeEditingId.value = row.id
  Object.assign(typeForm, { name: row.name, type: row.type, remark: row.remark ?? '' })
  typeDialogVisible.value = true
}

async function handleTypeSubmit(): Promise<void> {
  try {
    await typeFormRef.value?.validate()
  } catch {
    return
  }
  const payload = {
    name: typeForm.name.trim(),
    type: typeForm.type.trim(),
    remark: typeForm.remark || undefined,
  }
  if (typeIsEdit.value) {
    await updateDictType(typeEditingId.value, payload)
    ElMessage.success(t('common.saveSuccess'))
  } else {
    await createDictType(payload)
    ElMessage.success(t('common.saveSuccess'))
  }
  typeDialogVisible.value = false
  void fetchTypes()
}

async function handleTypeDelete(row: DictTypeVO): Promise<void> {
  try {
    await ElMessageBox.confirm(
      t('dict.typeDeleteConfirm', { name: row.name }),
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      },
    )
  } catch {
    return
  }
  await deleteDictType(row.id)
  ElMessage.success(t('common.deleteSuccess'))
  if (selectedType.value?.id === row.id) {
    selectedType.value = null
    dataRecords.value = []
    dataTotal.value = 0
  }
  void fetchTypes()
}

// ---------- 右側：字典數據 ----------

const dataLoading = ref(false)
const dataTotal = ref(0)
const dataRecords = ref<DictDataVO[]>([])
const dataQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

async function fetchData(): Promise<void> {
  if (!selectedType.value) {
    dataRecords.value = []
    dataTotal.value = 0
    return
  }
  dataLoading.value = true
  try {
    const keyword = dataQuery.keyword.trim()
    const data = await pageDictData({
      pageNum: dataQuery.pageNum,
      pageSize: dataQuery.pageSize,
      dictType: selectedType.value.type,
      keyword: keyword || undefined,
    })
    dataRecords.value = data.records
    dataTotal.value = data.total
  } finally {
    dataLoading.value = false
  }
}

function handleDataSearch(): void {
  dataQuery.pageNum = 1
  void fetchData()
}

function handleDataReset(): void {
  dataQuery.keyword = ''
  handleDataSearch()
}

function handleDataPageChange(): void {
  void fetchData()
}

const dataDialogVisible = ref(false)
const dataIsEdit = ref(false)
const dataEditingId = ref(0)
const dataFormRef = ref<FormInstance>()

const dataForm = reactive({ label: '', value: '', sortNo: 0, status: 1, remark: '' })

const dataRules: FormRules = {
  label: [{ required: true, message: () => t('dict.dataLabelRequired'), trigger: 'blur' }],
  value: [{ required: true, message: () => t('dict.dataValueRequired'), trigger: 'blur' }],
}

function openDataCreate(): void {
  if (!selectedType.value) {
    ElMessage.warning(t('dict.selectTypeFirst'))
    return
  }
  dataIsEdit.value = false
  dataEditingId.value = 0
  Object.assign(dataForm, { label: '', value: '', sortNo: 0, status: 1, remark: '' })
  dataDialogVisible.value = true
}

function openDataEdit(row: DictDataVO): void {
  dataIsEdit.value = true
  dataEditingId.value = row.id
  Object.assign(dataForm, {
    label: row.label,
    value: row.value,
    sortNo: row.sortNo,
    status: row.status,
    remark: row.remark ?? '',
  })
  dataDialogVisible.value = true
}

async function handleDataSubmit(): Promise<void> {
  try {
    await dataFormRef.value?.validate()
  } catch {
    return
  }
  if (!selectedType.value) {
    return
  }
  const payload = {
    dictType: selectedType.value.type,
    label: dataForm.label.trim(),
    value: dataForm.value.trim(),
    sortNo: Number.isFinite(dataForm.sortNo) ? dataForm.sortNo : 0,
    status: dataForm.status,
    remark: dataForm.remark || undefined,
  }
  if (dataIsEdit.value) {
    await updateDictData(dataEditingId.value, payload)
    ElMessage.success(t('common.saveSuccess'))
  } else {
    await createDictData(payload)
    ElMessage.success(t('common.saveSuccess'))
  }
  dataDialogVisible.value = false
  void fetchData()
}

async function handleDataDelete(row: DictDataVO): Promise<void> {
  try {
    await ElMessageBox.confirm(
      t('dict.dataDeleteConfirm', { name: row.label }),
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      },
    )
  } catch {
    return
  }
  await deleteDictData(row.id)
  ElMessage.success(t('common.deleteSuccess'))
  void fetchData()
}

onMounted(() => {
  void fetchTypes()
})
</script>

<template>
  <div class="dict-page">
    <el-row :gutter="16">
      <el-col :span="10">
        <el-card shadow="never" class="dict-card">
          <template #header>
            <div class="card-header">
              <span>{{ t('dict.typeTitle') }}</span>
              <el-button
                v-permission="'system:dict:add'"
                type="primary"
                size="small"
                :icon="Plus"
                @click="openTypeCreate"
              >
                {{ t('dict.typeAdd') }}
              </el-button>
            </div>
          </template>
          <div class="mini-toolbar">
            <el-input
              v-model="typeQuery.keyword"
              class="mini-search"
              size="small"
              :placeholder="t('dict.typeSearchPlaceholder')"
              clearable
              @keyup.enter="handleTypeSearch"
              @clear="handleTypeSearch"
            />
            <el-button size="small" type="primary" :icon="Search" @click="handleTypeSearch">
              {{ t('common.search') }}
            </el-button>
            <el-button size="small" :icon="Refresh" @click="handleTypeReset">{{ t('common.reset') }}</el-button>
          </div>
          <el-table
            v-loading="typeLoading"
            :data="typeRecords"
            size="small"
            highlight-current-row
            @current-change="handleTypeSelect"
          >
            <el-table-column prop="name" :label="t('dict.typeName')" min-width="110" show-overflow-tooltip />
            <el-table-column prop="type" :label="t('dict.typeType')" min-width="130" show-overflow-tooltip />
            <el-table-column :label="t('common.operation')" width="110">
              <template #default="{ row }">
                <el-button
                  v-permission="'system:dict:edit'"
                  link
                  type="primary"
                  size="small"
                  @click.stop="openTypeEdit(row as DictTypeVO)"
                >
                  {{ t('common.edit') }}
                </el-button>
                <el-button
                  v-permission="'system:dict:delete'"
                  link
                  type="danger"
                  size="small"
                  @click.stop="handleTypeDelete(row as DictTypeVO)"
                >
                  {{ t('common.delete') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mini-pager">
            <el-pagination
              v-model:current-page="typeQuery.pageNum"
              v-model:page-size="typeQuery.pageSize"
              :total="typeTotal"
              layout="total, prev, pager, next"
              small
              @current-change="handleTypePageChange"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never" class="dict-card">
          <template #header>
            <div class="card-header">
              <span>{{ t('dict.dataTitle') }}{{ selectedType ? ` - ${selectedType.name}` : '' }}</span>
              <el-button
                v-permission="'system:dict:add'"
                type="primary"
                size="small"
                :icon="Plus"
                :disabled="!selectedType"
                @click="openDataCreate"
              >
                {{ t('dict.dataAdd') }}
              </el-button>
            </div>
          </template>
          <template v-if="selectedType">
            <div class="mini-toolbar">
              <el-input
                v-model="dataQuery.keyword"
                class="mini-search"
                size="small"
                :placeholder="t('dict.dataSearchPlaceholder')"
                clearable
                @keyup.enter="handleDataSearch"
                @clear="handleDataSearch"
              />
              <el-button size="small" type="primary" :icon="Search" @click="handleDataSearch">
                {{ t('common.search') }}
              </el-button>
              <el-button size="small" :icon="Refresh" @click="handleDataReset">{{ t('common.reset') }}</el-button>
            </div>
            <el-table v-loading="dataLoading" :data="dataRecords" size="small">
              <el-table-column prop="label" :label="t('dict.dataLabel')" min-width="110" />
              <el-table-column prop="value" :label="t('dict.dataValue')" min-width="110" />
              <el-table-column prop="sortNo" :label="t('common.sortNo')" width="60" align="center" />
              <el-table-column :label="t('common.status')" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                    {{ row.status === 1 ? t('dict.statusNormal') : t('common.disabled') }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('common.remark')" min-width="110">
                <template #default="{ row }">{{ row.remark || '-' }}</template>
              </el-table-column>
              <el-table-column :label="t('common.operation')" width="110">
                <template #default="{ row }">
                  <el-button
                    v-permission="'system:dict:edit'"
                    link
                    type="primary"
                    size="small"
                    @click="openDataEdit(row as DictDataVO)"
                  >
                    {{ t('common.edit') }}
                  </el-button>
                  <el-button
                    v-permission="'system:dict:delete'"
                    link
                    type="danger"
                    size="small"
                    @click="handleDataDelete(row as DictDataVO)"
                  >
                    {{ t('common.delete') }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="mini-pager">
              <el-pagination
                v-model:current-page="dataQuery.pageNum"
                v-model:page-size="dataQuery.pageSize"
                :total="dataTotal"
                layout="total, prev, pager, next"
                small
                @current-change="handleDataPageChange"
              />
            </div>
          </template>
          <el-empty v-else :description="t('dict.selectTypeEmpty')" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="typeDialogVisible"
      :title="typeIsEdit ? t('dict.typeEditTitle') : t('dict.typeAddTitle')"
      width="460px"
      destroy-on-close
    >
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item :label="t('dict.typeName')" prop="name">
          <el-input v-model="typeForm.name" :placeholder="t('dict.typeNameRequired')" />
        </el-form-item>
        <el-form-item :label="t('dict.typeType')" prop="type">
          <el-input v-model="typeForm.type" :placeholder="t('dict.typeCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('common.remark')" prop="remark">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" :placeholder="t('dict.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleTypeSubmit">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="dataDialogVisible"
      :title="dataIsEdit ? t('dict.dataEditTitle') : t('dict.dataAddTitle')"
      width="480px"
      destroy-on-close
    >
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item :label="t('dict.ownerType')">
          <el-input :model-value="selectedType?.type" disabled />
        </el-form-item>
        <el-form-item :label="t('dict.dataLabel')" prop="label">
          <el-input v-model="dataForm.label" :placeholder="t('dict.dataLabelRequired')" />
        </el-form-item>
        <el-form-item :label="t('dict.dataValue')" prop="value">
          <el-input v-model="dataForm.value" :placeholder="t('dict.dataValueRequired')" />
        </el-form-item>
        <el-form-item :label="t('common.sortNo')" prop="sortNo">
          <el-input-number v-model="dataForm.sortNo" :min="0" />
        </el-form-item>
        <el-form-item :label="t('common.status')" prop="status">
          <el-switch v-model="dataForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item :label="t('common.remark')" prop="remark">
          <el-input v-model="dataForm.remark" type="textarea" :rows="2" :placeholder="t('dict.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleDataSubmit">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dict-card {
  height: 100%;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.mini-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.mini-search {
  width: 180px;
}
.mini-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
</style>
