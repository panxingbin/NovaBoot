<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { listMyProcesses } from '@/api/workflow'
import type { MyProcess } from '@/api/workflow'
import { formatDateTime } from '@/utils/format'

interface DataEntry {
  key: string
  value: string
}

const { t } = useI18n()

const loading = ref(false)
const records = ref<MyProcess[]>([])

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    records.value = await listMyProcesses()
  } finally {
    loading.value = false
  }
}

function dataEntries(formData: Record<string, unknown> | undefined): DataEntry[] {
  if (!formData) {
    return []
  }
  return Object.entries(formData).map(([key, value]) => ({
    key,
    value: value === null || value === undefined ? '' : String(value),
  }))
}

type StatusTagType = 'info' | 'success' | 'danger'

function statusTagType(record: MyProcess): StatusTagType {
  if (record.running) {
    return 'info'
  }
  if (record.approved === true) {
    return 'success'
  }
  if (record.approved === false) {
    return 'danger'
  }
  return 'info'
}

function statusText(record: MyProcess): string {
  if (record.running) {
    return t('flow.statusRunning')
  }
  if (record.approved === true) {
    return t('flow.statusApproved')
  }
  if (record.approved === false) {
    return t('flow.statusRejected')
  }
  return record.status || '-'
}

onMounted(() => {
  void fetchList()
})
</script>

<template>
  <el-card shadow="never">
    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column type="expand">
        <template #default="{ row }">
          <el-descriptions
            v-if="dataEntries((row as MyProcess).formData).length > 0"
            :column="1"
            border
            class="expand-descriptions"
          >
            <el-descriptions-item
              v-for="entry in dataEntries((row as MyProcess).formData)"
              :key="entry.key"
              :label="entry.key"
            >
              {{ entry.value }}
            </el-descriptions-item>
          </el-descriptions>
          <el-empty v-else :description="t('flow.formDataEmpty')" :image-size="60" />
        </template>
      </el-table-column>
      <el-table-column prop="formKey" :label="t('flow.formKey')" width="140" />
      <el-table-column :label="t('common.status')" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row as MyProcess)" disable-transitions>
            {{ statusText(row as MyProcess) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('flow.startTime')" width="170">
        <template #default="{ row }">{{ formatDateTime((row as MyProcess).startTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('flow.endTime')" width="170">
        <template #default="{ row }">{{ formatDateTime((row as MyProcess).endTime) }}</template>
      </el-table-column>
      <el-table-column
        prop="processInstanceId"
        :label="t('flow.processInstanceId')"
        min-width="200"
        show-overflow-tooltip
      />
    </el-table>
  </el-card>
</template>

<style scoped>
.expand-descriptions {
  margin: 8px 24px;
  width: auto;
}
</style>
