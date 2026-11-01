<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Refresh, Search } from '@element-plus/icons-vue'
import { pageLoginLogs } from '@/api/monitor'
import type { LoginLogVO } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const { t } = useI18n()

const loading = ref(false)
const total = ref(0)
const records = ref<LoginLogVO[]>([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  status: undefined as number | undefined,
})

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const data = await pageLoginLogs({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
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
  query.username = ''
  query.status = undefined
  handleSearch()
}

function handleSizeChange(): void {
  query.pageNum = 1
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
        v-model="query.username"
        class="search-input"
        :placeholder="t('loginlog.username')"
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
        <el-option :label="t('loginlog.statusSuccess')" :value="0" />
        <el-option :label="t('loginlog.statusFail')" :value="1" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">
        {{ t('common.search') }}
      </el-button>
      <el-button :icon="Refresh" @click="handleReset">{{ t('common.reset') }}</el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="username" :label="t('loginlog.username')" min-width="130" />
      <el-table-column prop="ip" :label="t('loginlog.ip')" min-width="140" />
      <el-table-column :label="t('common.status')" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? t('loginlog.statusSuccess') : t('loginlog.statusFail') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('loginlog.msg')" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.msg || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('loginlog.time')" width="180">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
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
</style>
