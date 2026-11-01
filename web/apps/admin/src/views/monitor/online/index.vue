<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { forceLogout, pageOnlineUsers } from '@/api/monitor'
import type { OnlineUserVO } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const { t } = useI18n()

const loading = ref(false)
const total = ref(0)
const records = ref<OnlineUserVO[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const keyword = query.keyword.trim()
    const data = await pageOnlineUsers({
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

async function handleForceLogout(row: OnlineUserVO): Promise<void> {
  try {
    await ElMessageBox.confirm(
      t('online.forceLogoutConfirm', { name: row.nickname || row.username }),
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
  await forceLogout(row.token)
  ElMessage.success(t('online.forceLogoutSuccess'))
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
        :placeholder="t('online.searchPlaceholder')"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">
        {{ t('common.search') }}
      </el-button>
      <el-button :icon="Refresh" @click="handleReset">{{ t('common.reset') }}</el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="username" :label="t('online.username')" min-width="120" />
      <el-table-column prop="nickname" :label="t('online.nickname')" min-width="120">
        <template #default="{ row }">{{ row.nickname || '-' }}</template>
      </el-table-column>
      <el-table-column prop="device" :label="t('online.device')" min-width="140">
        <template #default="{ row }">{{ row.device || '-' }}</template>
      </el-table-column>
      <el-table-column label="Token" min-width="110">
        <template #default="{ row }">{{ row.token ? row.token.slice(0, 8) : '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('online.loginTime')" width="180">
        <template #default="{ row }">{{ formatDateTime(row.loginTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="110" fixed="right">
        <template #default="{ row }">
          <el-button
            v-permission="'monitor:online:forceLogout'"
            link
            type="danger"
            @click="handleForceLogout(row as OnlineUserVO)"
          >
            {{ t('online.forceLogout') }}
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
