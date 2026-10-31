<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { deleteOperLog, pageOperLogs } from '@/api/monitor'
import type { OperLogVO } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const total = ref(0)
const records = ref<OperLogVO[]>([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  username: '',
  status: undefined as number | undefined,
})

function businessTypeMeta(type: number): { label: string; tag: 'info' | 'success' | 'warning' | 'danger' | 'primary' } {
  const meta: Record<number, { label: string; tag: 'info' | 'success' | 'warning' | 'danger' | 'primary' }> = {
    0: { label: '其它', tag: 'info' },
    1: { label: '新增', tag: 'success' },
    2: { label: '修改', tag: 'warning' },
    3: { label: '刪除', tag: 'danger' },
    4: { label: '授權', tag: 'primary' },
    5: { label: '強退', tag: 'danger' },
    6: { label: '上傳', tag: 'warning' },
  }
  return meta[type] ?? { label: '其它', tag: 'info' }
}

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const data = await pageOperLogs({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      title: query.title.trim() || undefined,
      username: query.username.trim() || undefined,
      status: query.status,
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
  query.title = ''
  query.username = ''
  query.status = undefined
  handleSearch()
}

function handleSizeChange(): void {
  query.pageNum = 1
  void fetchList()
}

// ---------- 詳情 ----------

const detailVisible = ref(false)
const detail = ref<OperLogVO | null>(null)

function openDetail(row: OperLogVO): void {
  detail.value = row
  detailVisible.value = true
}

// ---------- 刪除 ----------

async function handleDelete(row: OperLogVO): Promise<void> {
  try {
    await ElMessageBox.confirm(`確定要刪除操作日誌「${row.title}」嗎？`, '警告', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteOperLog(row.id)
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
        v-model="query.title"
        class="search-input"
        placeholder="模塊標題"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-input
        v-model="query.username"
        class="search-input"
        placeholder="操作人"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-select
        v-model="query.status"
        class="status-select"
        placeholder="狀態"
        clearable
        @change="handleSearch"
      >
        <el-option label="成功" :value="0" />
        <el-option label="失敗" :value="1" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="title" label="模塊" min-width="120" show-overflow-tooltip />
      <el-table-column label="類型" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="businessTypeMeta(row.businessType).tag" size="small">
            {{ businessTypeMeta(row.businessType).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operName" label="操作人" min-width="100" />
      <el-table-column label="URL" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.url || '-' }}</template>
      </el-table-column>
      <el-table-column prop="ip" label="IP" min-width="120" />
      <el-table-column label="狀態" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '成功' : '失敗' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="耗時" width="90" align="center">
        <template #default="{ row }">{{ row.costMs }} ms</template>
      </el-table-column>
      <el-table-column label="時間" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row as OperLogVO)">詳情</el-button>
          <el-button
            v-permission="'monitor:operlog:delete'"
            link
            type="danger"
            @click="handleDelete(row as OperLogVO)"
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

    <el-drawer v-model="detailVisible" title="操作日誌詳情" size="480px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="模塊標題">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="業務類型">
          <el-tag :type="businessTypeMeta(detail.businessType).tag" size="small">
            {{ businessTypeMeta(detail.businessType).label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operName }}</el-descriptions-item>
        <el-descriptions-item label="請求方式">{{ detail.requestMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="方法">{{ detail.method || '-' }}</el-descriptions-item>
        <el-descriptions-item label="URL">{{ detail.url || '-' }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ detail.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item label="狀態">
          <el-tag :type="detail.status === 0 ? 'success' : 'danger'" size="small">
            {{ detail.status === 0 ? '成功' : '失敗' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗時">{{ detail.costMs }} ms</el-descriptions-item>
        <el-descriptions-item label="時間">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detail" class="detail-block">
        <div class="detail-label">請求參數</div>
        <pre class="detail-pre">{{ detail.param || '-' }}</pre>
        <div class="detail-label">錯誤信息</div>
        <pre class="detail-pre">{{ detail.errorMsg || '-' }}</pre>
      </div>
    </el-drawer>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.search-input {
  width: 200px;
}
.status-select {
  width: 120px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.detail-block {
  margin-top: 16px;
}
.detail-label {
  font-weight: 600;
  margin: 10px 0 6px;
  color: #303133;
}
.detail-pre {
  margin: 0;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  line-height: 1.6;
  max-height: 220px;
  overflow: auto;
}
</style>
