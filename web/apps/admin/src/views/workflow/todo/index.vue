<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { completeTask, listTodoTasks } from '@/api/workflow'
import type { TodoTask } from '@/api/workflow'
import { formatDateTime } from '@/utils/format'

interface DataEntry {
  key: string
  value: string
}

const { t } = useI18n()

const loading = ref(false)
const tasks = ref<TodoTask[]>([])

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    tasks.value = await listTodoTasks()
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

// ---------- 審批對話框 ----------

const dialogVisible = ref(false)
const currentTask = ref<TodoTask | null>(null)
const comment = ref('')
const submitting = ref(false)

function openReview(task: TodoTask): void {
  currentTask.value = task
  comment.value = ''
  dialogVisible.value = true
}

async function submitReview(approved: boolean): Promise<void> {
  const task = currentTask.value
  if (!task) {
    return
  }
  submitting.value = true
  try {
    await completeTask(task.taskId, {
      approved,
      comment: comment.value.trim() || undefined,
    })
    ElMessage.success(t(approved ? 'flow.approveSuccess' : 'flow.rejectSuccess'))
    dialogVisible.value = false
    void fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void fetchList()
})
</script>

<template>
  <el-card shadow="never">
    <el-table v-loading="loading" :data="tasks" stripe>
      <el-table-column prop="taskName" :label="t('flow.taskName')" min-width="140" show-overflow-tooltip />
      <el-table-column prop="formKey" :label="t('flow.formKey')" width="120" />
      <el-table-column :label="t('flow.formData')" min-width="240">
        <template #default="{ row }">
          <div v-for="entry in dataEntries((row as TodoTask).formData)" :key="entry.key" class="data-line">
            {{ entry.key }}: {{ entry.value }}
          </div>
          <span v-if="dataEntries((row as TodoTask).formData).length === 0">-</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('flow.createTime')" width="170">
        <template #default="{ row }">{{ formatDateTime((row as TodoTask).createTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openReview(row as TodoTask)">
            {{ t('flow.review') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="currentTask ? t('flow.reviewTitle', { name: currentTask.taskName }) : t('flow.review')"
      width="560px"
      destroy-on-close
    >
      <el-descriptions
        v-if="currentTask && dataEntries(currentTask.formData).length > 0"
        :column="1"
        border
      >
        <el-descriptions-item
          v-for="entry in dataEntries(currentTask.formData)"
          :key="entry.key"
          :label="entry.key"
        >
          {{ entry.value }}
        </el-descriptions-item>
      </el-descriptions>
      <el-empty v-else :description="t('flow.formDataEmpty')" :image-size="60" />

      <el-form label-width="90px" class="comment-form" @submit.prevent>
        <el-form-item :label="t('flow.comment')">
          <el-input
            v-model="comment"
            type="textarea"
            :rows="3"
            :placeholder="t('flow.commentPlaceholder')"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button
          v-permission="'workflow:approve'"
          type="danger"
          :loading="submitting"
          @click="submitReview(false)"
        >
          {{ t('flow.reject') }}
        </el-button>
        <el-button
          v-permission="'workflow:approve'"
          type="primary"
          :loading="submitting"
          @click="submitReview(true)"
        >
          {{ t('flow.approve') }}
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.data-line {
  line-height: 1.8;
}
.comment-form {
  margin-top: 16px;
}
</style>
