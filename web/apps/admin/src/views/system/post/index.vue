<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { createPost, deletePost, pagePosts, updatePost } from '@/api/system'
import type { PostVO } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const { t } = useI18n()

const loading = ref(false)
const total = ref(0)
const records = ref<PostVO[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const keyword = query.keyword.trim()
    const data = await pagePosts({
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
  code: '',
  name: '',
  sortNo: 0,
  status: 1,
  remark: '',
})

const rules: FormRules = {
  code: [{ required: true, message: () => t('post.codeRequired'), trigger: 'blur' }],
  name: [{ required: true, message: () => t('post.nameRequired'), trigger: 'blur' }],
}

function openCreate(): void {
  isEdit.value = false
  editingId.value = 0
  Object.assign(form, { code: '', name: '', sortNo: 0, status: 1, remark: '' })
  dialogVisible.value = true
}

function openEdit(row: PostVO): void {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    code: row.code,
    name: row.name,
    sortNo: row.sortNo,
    status: row.status,
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
    code: form.code.trim(),
    name: form.name.trim(),
    sortNo: Number.isFinite(form.sortNo) ? form.sortNo : 0,
    status: form.status,
    remark: form.remark || undefined,
  }
  if (isEdit.value) {
    await updatePost(editingId.value, payload)
    ElMessage.success(t('common.saveSuccess'))
  } else {
    await createPost(payload)
    ElMessage.success(t('common.saveSuccess'))
  }
  dialogVisible.value = false
  void fetchList()
}

// ---------- 刪除 ----------

async function handleDelete(row: PostVO): Promise<void> {
  try {
    await ElMessageBox.confirm(t('post.deleteConfirm', { name: row.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    })
  } catch {
    return
  }
  await deletePost(row.id)
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
        :placeholder="t('post.searchPlaceholder')"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">{{ t('common.search') }}</el-button>
      <el-button :icon="Refresh" @click="handleReset">{{ t('common.reset') }}</el-button>
      <el-button v-permission="'system:post:add'" type="primary" :icon="Plus" @click="openCreate">
        {{ t('post.addPost') }}
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="code" :label="t('post.code')" min-width="120" />
      <el-table-column prop="name" :label="t('post.name')" min-width="140" />
      <el-table-column prop="sortNo" :label="t('common.sortNo')" width="70" />
      <el-table-column :label="t('common.status')" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? t('post.statusNormal') : t('common.disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.remark')" min-width="140">
        <template #default="{ row }">{{ row.remark || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('common.createdAt')" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="150" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:post:edit'" link type="primary" @click="openEdit(row as PostVO)">
            {{ t('common.edit') }}
          </el-button>
          <el-button v-permission="'system:post:delete'" link type="danger" @click="handleDelete(row as PostVO)">
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
      :title="isEdit ? t('post.editPost') : t('post.addPost')"
      width="480px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="t('post.code')" prop="code">
          <el-input v-model="form.code" :placeholder="t('post.codeRequired')" />
        </el-form-item>
        <el-form-item :label="t('post.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('post.nameRequired')" />
        </el-form-item>
        <el-form-item :label="t('common.sortNo')" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="0" />
        </el-form-item>
        <el-form-item :label="t('common.status')" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item :label="t('common.remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" :placeholder="t('post.remarkPlaceholder')" />
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
