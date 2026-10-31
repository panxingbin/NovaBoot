<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createNotice, deleteNotice, getNotice, pageNotices, updateNotice } from '@/api/system'
import type { NoticeVO } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const total = ref(0)
const records = ref<NoticeVO[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

function noticeTypeMeta(type: number): { label: string; tag: 'primary' | 'warning' } {
  return type === 2 ? { label: '公告', tag: 'warning' } : { label: '通知', tag: 'primary' }
}

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const keyword = query.keyword.trim()
    const data = await pageNotices({
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

// ---------- 查看 ----------

const detailVisible = ref(false)
const detail = ref<NoticeVO | null>(null)

async function openDetail(row: NoticeVO): Promise<void> {
  detail.value = await getNotice(row.id)
  detailVisible.value = true
}

// ---------- 新增 / 修改 ----------

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(0)
const formRef = ref<FormInstance>()

const form = reactive({
  title: '',
  type: 1,
  status: 1,
  content: '',
  remark: '',
})

const rules: FormRules = {
  title: [{ required: true, message: '請輸入公告標題', trigger: 'blur' }],
  content: [{ required: true, message: '請輸入公告內容', trigger: 'blur' }],
}

function openCreate(): void {
  isEdit.value = false
  editingId.value = 0
  Object.assign(form, { title: '', type: 1, status: 1, content: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row: NoticeVO): void {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    type: row.type,
    status: row.status,
    content: row.content ?? '',
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
    title: form.title.trim(),
    type: form.type,
    status: form.status,
    content: form.content,
    remark: form.remark || undefined,
  }
  if (isEdit.value) {
    await updateNotice(editingId.value, payload)
    ElMessage.success('修改成功')
  } else {
    await createNotice(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  void fetchList()
}

// ---------- 刪除 ----------

async function handleDelete(row: NoticeVO): Promise<void> {
  try {
    await ElMessageBox.confirm(`確定要刪除公告「${row.title}」嗎？`, '警告', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteNotice(row.id)
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
        placeholder="公告標題"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <el-button v-permission="'system:notice:add'" type="primary" :icon="Plus" @click="openCreate">
        新增公告
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="title" label="標題" min-width="200" show-overflow-tooltip />
      <el-table-column label="類型" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="noticeTypeMeta(row.type).tag">{{ noticeTypeMeta(row.type).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="狀態" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '正常' : '關閉' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="創建時間" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row as NoticeVO)">查看</el-button>
          <el-button
            v-permission="'system:notice:edit'"
            link
            type="primary"
            @click="openEdit(row as NoticeVO)"
          >
            修改
          </el-button>
          <el-button
            v-permission="'system:notice:delete'"
            link
            type="danger"
            @click="handleDelete(row as NoticeVO)"
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

    <el-dialog v-model="detailVisible" :title="detail?.title ?? '公告詳情'" width="560px">
      <div v-if="detail" class="notice-detail">
        <div class="notice-meta">
          <el-tag :type="noticeTypeMeta(detail.type).tag">
            {{ noticeTypeMeta(detail.type).label }}
          </el-tag>
          <span class="notice-time">{{ formatDateTime(detail.createdAt) }}</span>
        </div>
        <div class="notice-content">{{ detail.content }}</div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改公告' : '新增公告'"
      width="600px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="標題" prop="title">
          <el-input v-model="form.title" placeholder="請輸入公告標題" />
        </el-form-item>
        <el-form-item label="類型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio-button :value="1">通知</el-radio-button>
            <el-radio-button :value="2">公告</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="狀態" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="內容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="請輸入公告內容"
          />
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
.notice-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.notice-time {
  color: #909399;
  font-size: 13px;
}
.notice-content {
  white-space: pre-wrap;
  line-height: 1.7;
  color: #303133;
}
</style>
