<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { createConfig, deleteConfig, pageConfigs, updateConfig } from '@/api/system'
import type { ConfigVO } from '@/api/types'

const { t } = useI18n()

const loading = ref(false)
const total = ref(0)
const records = ref<ConfigVO[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const keyword = query.keyword.trim()
    const data = await pageConfigs({
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

// ---------- 新增 / 修改 ----------

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(0)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  key: '',
  value: '',
  remark: '',
})

const rules: FormRules = {
  name: [{ required: true, message: () => t('config.nameRequired'), trigger: 'blur' }],
  key: [{ required: true, message: () => t('config.keyRequired'), trigger: 'blur' }],
  value: [{ required: true, message: () => t('config.valueRequired'), trigger: 'blur' }],
}

function openCreate(): void {
  isEdit.value = false
  editingId.value = 0
  Object.assign(form, { name: '', key: '', value: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row: ConfigVO): void {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    key: row.key,
    value: row.value,
    remark: row.remark ?? '',
  })
  dialogVisible.value = true
}

async function handleSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  const payload = {
    name: form.name.trim(),
    key: form.key.trim(),
    value: form.value,
    remark: form.remark || undefined,
  }
  if (isEdit.value) {
    await updateConfig(editingId.value, payload)
    ElMessage.success(t('common.saveSuccess'))
  } else {
    await createConfig(payload)
    ElMessage.success(t('common.saveSuccess'))
  }
  dialogVisible.value = false
  void fetchList()
}

// ---------- 刪除 ----------

async function handleDelete(row: ConfigVO): Promise<void> {
  try {
    await ElMessageBox.confirm(t('config.deleteConfirm', { name: row.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteConfig(row.id)
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
        :placeholder="t('config.searchPlaceholder')"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">{{ t('common.search') }}</el-button>
      <el-button :icon="Refresh" @click="handleReset">{{ t('common.reset') }}</el-button>
      <el-button v-permission="'system:config:add'" type="primary" :icon="Plus" @click="openCreate">
        {{ t('config.addConfig') }}
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="name" :label="t('config.name')" min-width="150" />
      <el-table-column prop="key" :label="t('config.key')" min-width="160" />
      <el-table-column prop="value" :label="t('config.value')" min-width="150" show-overflow-tooltip />
      <el-table-column :label="t('config.builtin')" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.builtin" type="warning">{{ t('config.builtin') }}</el-tag>
          <el-tag v-else type="info">{{ t('config.custom') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.remark')" min-width="140">
        <template #default="{ row }">{{ row.remark || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="150" fixed="right">
        <template #default="{ row }">
          <el-button
            v-permission="'system:config:edit'"
            link
            type="primary"
            @click="openEdit(row as ConfigVO)"
          >
            {{ t('common.edit') }}
          </el-button>
          <el-button
            v-permission="'system:config:delete'"
            link
            type="danger"
            @click="handleDelete(row as ConfigVO)"
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
      :title="isEdit ? t('config.editConfig') : t('config.addConfig')"
      width="480px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="t('config.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('config.nameRequired')" />
        </el-form-item>
        <el-form-item :label="t('config.key')" prop="key">
          <el-input v-model="form.key" :placeholder="t('config.keyPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('config.value')" prop="value">
          <el-input v-model="form.value" :placeholder="t('config.valueRequired')" />
        </el-form-item>
        <el-form-item :label="t('common.remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" :placeholder="t('config.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ t('common.confirm') }}</el-button>
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
</style>
