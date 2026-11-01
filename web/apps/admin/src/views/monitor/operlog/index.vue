<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { deleteOperLog, pageOperLogs } from '@/api/monitor'
import type { OperLogVO } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const { t } = useI18n()

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

const businessTypeTags: Record<number, 'info' | 'success' | 'warning' | 'danger' | 'primary'> = {
  0: 'info',
  1: 'success',
  2: 'warning',
  3: 'danger',
  4: 'primary',
  5: 'danger',
  6: 'warning',
}

const businessTypeLabels = computed<Record<number, string>>(() => ({
  0: t('operlog.typeOther'),
  1: t('operlog.typeCreate'),
  2: t('operlog.typeUpdate'),
  3: t('operlog.typeDelete'),
  4: t('operlog.typeGrant'),
  5: t('operlog.typeForce'),
  6: t('operlog.typeUpload'),
}))

function businessTypeMeta(type: number): { label: string; tag: 'info' | 'success' | 'warning' | 'danger' | 'primary' } {
  return {
    label: businessTypeLabels.value[type] ?? t('operlog.typeOther'),
    tag: businessTypeTags[type] ?? 'info',
  }
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
    await ElMessageBox.confirm(
      t('operlog.deleteConfirm', { name: row.title }),
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
  await deleteOperLog(row.id)
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
        v-model="query.title"
        class="search-input"
        :placeholder="t('operlog.moduleTitle')"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-input
        v-model="query.username"
        class="search-input"
        :placeholder="t('operlog.operName')"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-select
        v-model="query.status"
        class="status-select"
        :placeholder="t('common.status')"
        clearable
        @change="handleSearch"
      >
        <el-option :label="t('operlog.statusSuccess')" :value="0" />
        <el-option :label="t('operlog.statusFail')" :value="1" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">
        {{ t('common.search') }}
      </el-button>
      <el-button :icon="Refresh" @click="handleReset">{{ t('common.reset') }}</el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="title" :label="t('operlog.module')" min-width="120" show-overflow-tooltip />
      <el-table-column :label="t('operlog.type')" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="businessTypeMeta(row.businessType).tag" size="small">
            {{ businessTypeMeta(row.businessType).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operName" :label="t('operlog.operName')" min-width="100" />
      <el-table-column :label="t('operlog.url')" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.url || '-' }}</template>
      </el-table-column>
      <el-table-column prop="ip" :label="t('operlog.ip')" min-width="120" />
      <el-table-column :label="t('common.status')" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? t('operlog.statusSuccess') : t('operlog.statusFail') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('operlog.costMs')" width="90" align="center">
        <template #default="{ row }">{{ row.costMs }} ms</template>
      </el-table-column>
      <el-table-column :label="t('operlog.time')" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row as OperLogVO)">
            {{ t('operlog.detail') }}
          </el-button>
          <el-button
            v-permission="'monitor:operlog:delete'"
            link
            type="danger"
            @click="handleDelete(row as OperLogVO)"
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

    <el-drawer v-model="detailVisible" :title="t('operlog.detailTitle')" size="480px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item :label="t('operlog.moduleTitle')">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item :label="t('operlog.businessType')">
          <el-tag :type="businessTypeMeta(detail.businessType).tag" size="small">
            {{ businessTypeMeta(detail.businessType).label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('operlog.operName')">{{ detail.operName }}</el-descriptions-item>
        <el-descriptions-item :label="t('operlog.requestMethod')">{{ detail.requestMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('operlog.method')">{{ detail.method || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('operlog.url')">{{ detail.url || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('operlog.ip')">{{ detail.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('common.status')">
          <el-tag :type="detail.status === 0 ? 'success' : 'danger'" size="small">
            {{ detail.status === 0 ? t('operlog.statusSuccess') : t('operlog.statusFail') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('operlog.costMs')">{{ detail.costMs }} ms</el-descriptions-item>
        <el-descriptions-item :label="t('operlog.time')">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detail" class="detail-block">
        <div class="detail-label">{{ t('operlog.param') }}</div>
        <pre class="detail-pre">{{ detail.param || '-' }}</pre>
        <div class="detail-label">{{ t('operlog.errorMsg') }}</div>
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
  color: var(--el-text-color-primary);
}
.detail-pre {
  margin: 0;
  padding: 10px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  line-height: 1.6;
  max-height: 220px;
  overflow: auto;
}
</style>
