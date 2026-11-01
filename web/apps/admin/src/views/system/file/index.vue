<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Upload } from '@element-plus/icons-vue'
import { deleteFile, downloadFile, pageFiles, uploadFile } from '@/api/system'
import type { FileVO } from '@/api/types'
import { formatDateTime, formatFileSize } from '@/utils/format'

const { t } = useI18n()

const loading = ref(false)
const uploading = ref(false)
const total = ref(0)
const records = ref<FileVO[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const keyword = query.keyword.trim()
    const data = await pageFiles({
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

async function handleUpload(options: { file: File }): Promise<void> {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    await uploadFile(formData)
    ElMessage.success(t('file.uploadSuccess'))
    void fetchList()
  } finally {
    uploading.value = false
  }
}

async function handleDownload(row: FileVO): Promise<void> {
  try {
    await downloadFile(row.id, row.originalName)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('file.downloadFailed'))
  }
}

async function handleDelete(row: FileVO): Promise<void> {
  try {
    await ElMessageBox.confirm(
      t('file.deleteConfirm', { name: row.originalName }),
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
  await deleteFile(row.id)
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
        :placeholder="t('file.searchPlaceholder')"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">
        {{ t('common.search') }}
      </el-button>
      <el-button :icon="Refresh" @click="handleReset">{{ t('common.reset') }}</el-button>
      <el-upload
        class="uploader"
        :show-file-list="false"
        multiple
        :http-request="handleUpload"
      >
        <el-button v-permission="'system:file:upload'" type="success" :icon="Upload" :loading="uploading">
          {{ t('file.upload') }}
        </el-button>
      </el-upload>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="originalName" :label="t('file.originalName')" min-width="200" show-overflow-tooltip />
      <el-table-column :label="t('file.size')" width="110">
        <template #default="{ row }">{{ formatFileSize(row.size) }}</template>
      </el-table-column>
      <el-table-column :label="t('file.contentType')" min-width="140">
        <template #default="{ row }">{{ row.contentType || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('file.uploadTime')" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="140" fixed="right">
        <template #default="{ row }">
          <el-link
            type="primary"
            :underline="false"
            class="download-link"
            @click="handleDownload(row as FileVO)"
          >
            {{ t('file.download') }}
          </el-link>
          <el-button
            v-permission="'system:file:delete'"
            link
            type="danger"
            @click="handleDelete(row as FileVO)"
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
.uploader {
  display: inline-block;
}
.download-link {
  margin-right: 12px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
