<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Download, View } from '@element-plus/icons-vue'
import { downloadGenZip, getGenMeta, listGenTables, previewGen } from '@/api/gen'
import type { GenColumnVO, GenOptionsBody, GenTableMetaVO, GenTableVO } from '@/api/gen'

const { t } = useI18n()

const DEFAULT_AUTHOR = 'novaboot'
const DEFAULT_MENU_START_ID = 1000
const KNOWN_PREFIXES = ['sys_', 'gen_', 'tool_', 'monitor_', 'biz_', 'nova_', 't_', 'tb_']

// ---------- 選項表單 ----------

const formRef = ref<FormInstance>()
const form = reactive({
  tableName: '',
  moduleName: '',
  businessName: '',
  functionName: '',
  author: DEFAULT_AUTHOR,
  menuStartId: DEFAULT_MENU_START_ID as number | undefined,
})

const rules = computed<FormRules>(() => ({
  tableName: [{ required: true, message: () => t('gen.tableNameRequired'), trigger: 'change' }],
  moduleName: [{ required: true, message: () => t('gen.moduleNameRequired'), trigger: 'blur' }],
  businessName: [{ required: true, message: () => t('gen.businessNameRequired'), trigger: 'blur' }],
}))

// ---------- 資料表 ----------

const tables = ref<GenTableVO[]>([])
const tablesLoading = ref(false)
const metaLoading = ref(false)
const meta = ref<GenTableMetaVO | null>(null)

async function fetchTables(): Promise<void> {
  tablesLoading.value = true
  try {
    tables.value = await listGenTables()
  } catch {
    // 錯誤提示由 http 層統一彈出
  } finally {
    tablesLoading.value = false
  }
}

async function fetchMeta(): Promise<void> {
  const requested = form.tableName
  metaLoading.value = true
  try {
    const data = await getGenMeta(requested)
    if (form.tableName === requested) {
      meta.value = data
    }
  } catch {
    if (form.tableName === requested) {
      meta.value = null
      // 錯誤提示由 http 層統一彈出
    }
  } finally {
    metaLoading.value = false
  }
}

function stripKnownPrefix(tableName: string): string {
  for (const prefix of KNOWN_PREFIXES) {
    if (tableName.startsWith(prefix)) {
      return tableName.slice(prefix.length)
    }
  }
  return tableName
}

function toLowerCamel(name: string): string {
  const parts = name.split('_').filter((part) => part.length > 0)
  if (parts.length === 0) {
    return ''
  }
  return parts
    .map((part, index) =>
      index === 0 ? part.toLowerCase() : part.charAt(0).toUpperCase() + part.slice(1).toLowerCase(),
    )
    .join('')
}

// 與用戶手改過的值共存：僅覆蓋仍為空或等於上一次建議值的字段
const lastSuggest = reactive({ moduleName: '', businessName: '', functionName: '' })

function handleTableChange(tableName: string): void {
  meta.value = null
  if (!tableName) {
    return
  }
  const stem = toLowerCamel(stripKnownPrefix(tableName))
  if (stem) {
    const suggestedModule = stem.toLowerCase()
    const suggestedBusiness = stem
    if (!form.moduleName || form.moduleName === lastSuggest.moduleName) {
      form.moduleName = suggestedModule
    }
    if (!form.businessName || form.businessName === lastSuggest.businessName) {
      form.businessName = suggestedBusiness
    }
    lastSuggest.moduleName = suggestedModule
    lastSuggest.businessName = suggestedBusiness
  }
  const selected = tables.value.find((item) => item.tableName === tableName)
  const comment = (selected?.tableComment || '').trim()
  if (comment && (!form.functionName || form.functionName === lastSuggest.functionName)) {
    form.functionName = comment
  }
  lastSuggest.functionName = comment
  void fetchMeta()
}

// ---------- 預覽 / 下載 ----------

async function ensureValid(): Promise<boolean> {
  try {
    await formRef.value?.validate()
  } catch {
    return false
  }
  return true
}

function buildBody(): GenOptionsBody {
  return {
    tableName: form.tableName,
    moduleName: form.moduleName.trim(),
    businessName: form.businessName.trim(),
    functionName: form.functionName.trim(),
    author: form.author.trim() || DEFAULT_AUTHOR,
    menuStartId: form.menuStartId ?? DEFAULT_MENU_START_ID,
  }
}

const previewVisible = ref(false)
const previewLoading = ref(false)
const previewEntries = ref<Array<{ path: string; content: string }>>([])
const activeTab = ref('')

async function handlePreview(): Promise<void> {
  if (!(await ensureValid())) {
    return
  }
  previewLoading.value = true
  try {
    const files = await previewGen(buildBody())
    previewEntries.value = Object.keys(files)
      .sort()
      .map((path) => ({ path, content: files[path] }))
    activeTab.value = previewEntries.value.length > 0 ? previewEntries.value[0].path : ''
    previewVisible.value = true
  } finally {
    previewLoading.value = false
  }
}

const downloadLoading = ref(false)

async function handleDownload(): Promise<void> {
  if (!(await ensureValid())) {
    return
  }
  downloadLoading.value = true
  try {
    await downloadGenZip(buildBody())
    ElMessage.success(t('gen.downloadSuccess'))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('gen.downloadFailed'))
  } finally {
    downloadLoading.value = false
  }
}

function fileNameOf(path: string): string {
  const index = path.lastIndexOf('/')
  return index >= 0 ? path.slice(index + 1) : path
}

onMounted(() => {
  void fetchTables()
})
</script>

<template>
  <el-card shadow="never">
    <div class="section-title">{{ t('gen.genOptions') }}</div>
    <el-form ref="formRef" :model="form" :rules="rules" inline class="gen-form">
      <el-form-item :label="t('gen.tableName')" prop="tableName">
        <el-select
          v-model="form.tableName"
          class="w-table"
          :loading="tablesLoading"
          filterable
          clearable
          :placeholder="t('gen.tableNamePlaceholder')"
          @change="handleTableChange"
        >
          <el-option
            v-for="table in tables"
            :key="table.tableName"
            :label="table.tableName"
            :value="table.tableName"
          >
            <span class="option-name">{{ table.tableName }}</span>
            <span class="option-comment">{{ table.tableComment || '-' }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item :label="t('gen.moduleName')" prop="moduleName">
        <el-input v-model="form.moduleName" class="w-field" :placeholder="t('gen.moduleNamePlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('gen.businessName')" prop="businessName">
        <el-input v-model="form.businessName" class="w-field" :placeholder="t('gen.businessNamePlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('gen.functionName')">
        <el-input v-model="form.functionName" class="w-field" :placeholder="t('gen.functionNamePlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('gen.author')">
        <el-input v-model="form.author" class="w-field" :placeholder="t('gen.authorPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('gen.menuStartId')">
        <el-input-number
          v-model="form.menuStartId"
          class="w-number"
          :min="1"
          :step="1"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item>
        <el-button
          v-permission="'tool:gen:preview'"
          type="primary"
          :icon="View"
          :loading="previewLoading"
          :disabled="!form.tableName"
          @click="handlePreview"
        >
          {{ t('gen.preview') }}
        </el-button>
        <el-button
          v-permission="'tool:gen:download'"
          type="success"
          :icon="Download"
          :loading="downloadLoading"
          :disabled="!form.tableName"
          @click="handleDownload"
        >
          {{ t('gen.download') }}
        </el-button>
      </el-form-item>
    </el-form>

    <template v-if="form.tableName">
      <div class="section-title">
        {{ t('gen.columns') }}
        <span class="section-table">{{ form.tableName }}</span>
      </div>
      <el-table v-loading="metaLoading" :data="meta?.columns ?? []" stripe>
        <el-table-column prop="columnName" :label="t('gen.columnName')" min-width="140" />
        <el-table-column prop="javaName" :label="t('gen.javaName')" min-width="120" />
        <el-table-column prop="javaType" :label="t('gen.javaType')" min-width="110" />
        <el-table-column prop="columnType" :label="t('gen.columnType')" min-width="120" />
        <el-table-column :label="t('gen.comment')" min-width="160">
          <template #default="{ row }">{{ (row as GenColumnVO).comment || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('gen.pk')" width="90">
          <template #default="{ row }">
            <el-tag v-if="(row as GenColumnVO).pk" type="success" size="small">PK</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <el-dialog
      v-model="previewVisible"
      :title="t('gen.previewTitle')"
      width="900px"
      top="6vh"
      destroy-on-close
    >
      <el-tabs v-if="previewEntries.length > 0" v-model="activeTab">
        <el-tab-pane
          v-for="entry in previewEntries"
          :key="entry.path"
          :name="entry.path"
          :label="fileNameOf(entry.path)"
          lazy
        >
          <div class="code-path">{{ entry.path }}</div>
          <pre class="code-view">{{ entry.content }}</pre>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="previewVisible = false">{{ t('gen.close') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.section-title {
  font-weight: 600;
  margin-bottom: 12px;
}
.section-table {
  margin-left: 8px;
  font-weight: 400;
  color: var(--el-text-color-secondary);
}
.gen-form {
  margin-bottom: 6px;
}
.w-table {
  width: 300px;
}
.w-field {
  width: 170px;
}
.w-number {
  width: 160px;
}
.option-name {
  margin-right: 16px;
}
.option-comment {
  float: right;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.code-path {
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  word-break: break-all;
}
.code-view {
  margin: 0;
  padding: 12px;
  max-height: 60vh;
  overflow: auto;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre;
  tab-size: 4;
}
</style>
