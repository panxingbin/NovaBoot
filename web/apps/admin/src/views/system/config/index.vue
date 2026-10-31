<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createConfig, deleteConfig, pageConfigs, updateConfig } from '@/api/system'
import type { ConfigVO } from '@/api/types'

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
  name: [{ required: true, message: '請輸入參數名稱', trigger: 'blur' }],
  key: [{ required: true, message: '請輸入參數鍵', trigger: 'blur' }],
  value: [{ required: true, message: '請輸入參數值', trigger: 'blur' }],
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
    ElMessage.success('修改成功')
  } else {
    await createConfig(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  void fetchList()
}

// ---------- 刪除 ----------

async function handleDelete(row: ConfigVO): Promise<void> {
  try {
    await ElMessageBox.confirm(`確定要刪除參數「${row.name}」嗎？`, '警告', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteConfig(row.id)
  ElMessage.success('刪除成功')
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
        placeholder="名稱 / 鍵"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <el-button v-permission="'system:config:add'" type="primary" :icon="Plus" @click="openCreate">
        新增參數
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="name" label="名稱" min-width="150" />
      <el-table-column prop="key" label="鍵" min-width="160" />
      <el-table-column prop="value" label="值" min-width="150" show-overflow-tooltip />
      <el-table-column label="內置" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.builtin" type="warning">內置</el-tag>
          <el-tag v-else type="info">自定義</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="備註" min-width="140">
        <template #default="{ row }">{{ row.remark || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button
            v-permission="'system:config:edit'"
            link
            type="primary"
            @click="openEdit(row as ConfigVO)"
          >
            修改
          </el-button>
          <el-button
            v-permission="'system:config:delete'"
            link
            type="danger"
            @click="handleDelete(row as ConfigVO)"
          >
            刪除
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
      :title="isEdit ? '修改參數' : '新增參數'"
      width="480px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名稱" prop="name">
          <el-input v-model="form.name" placeholder="請輸入參數名稱" />
        </el-form-item>
        <el-form-item label="鍵" prop="key">
          <el-input v-model="form.key" placeholder="請輸入參數鍵，如 sys.index.skin" />
        </el-form-item>
        <el-form-item label="值" prop="value">
          <el-input v-model="form.value" placeholder="請輸入參數值" />
        </el-form-item>
        <el-form-item label="備註" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="請輸入備註" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">確定</el-button>
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
