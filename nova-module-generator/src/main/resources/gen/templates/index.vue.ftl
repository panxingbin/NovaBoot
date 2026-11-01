<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  create${options.className},
  delete${options.className},
  page${options.className}s,
  update${options.className},
} from '@/api/${options.businessName}'
import type { ${options.className} } from '@/api/${options.businessName}'

// 代碼生成腳手架：欄位、文案與接口細節請按業務需要調整
const loading = ref(false)
const total = ref(0)
const records = ref<${options.className}[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const dialog = reactive({ visible: false, editing: false })
const form = reactive<Partial<${options.className}>>({})
const submitting = ref(false)

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const data = await page${options.className}s(query)
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

function resetForm(): void {
  Object.keys(form).forEach((key) => delete form[key])
}

function openCreate(): void {
  resetForm()
  dialog.editing = false
  dialog.visible = true
}

function openEdit(row: ${options.className}): void {
  resetForm()
  Object.assign(form, row)
  dialog.editing = true
  dialog.visible = true
}

async function handleSubmit(): Promise<void> {
  submitting.value = true
  try {
    const body = form as ${options.className}
    if (dialog.editing && form.id != null) {
      await update${options.className}(form.id, body)
    } else {
      await create${options.className}(body)
    }
    ElMessage.success('保存成功')
    dialog.visible = false
    void fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: ${options.className}): Promise<void> {
  try {
    await ElMessageBox.confirm('確定要刪除該條數據嗎？', '警告', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await delete${options.className}(row.id)
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
<#if table.searchColumn??>
      <el-input
        v-model="query.keyword"
        class="search-input"
        placeholder="${table.searchColumn.comment!table.searchColumn.columnName}"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
</#if>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <el-button
        v-permission="'${options.moduleName}:${options.businessName}:add'"
        class="add-btn"
        type="success"
        :icon="Plus"
        @click="openCreate"
      >
        新增
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
<#list table.columns as col>
  <#if col.columnName != "deleted">
      <el-table-column prop="${col.javaName}" label="${col.comment!col.javaName}" min-width="120" show-overflow-tooltip />
  </#if>
</#list>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button
            v-permission="'${options.moduleName}:${options.businessName}:edit'"
            link
            type="primary"
            @click="openEdit(row as ${options.className})"
          >
            修改
          </el-button>
          <el-button
            v-permission="'${options.moduleName}:${options.businessName}:delete'"
            link
            type="danger"
            @click="handleDelete(row as ${options.className})"
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
        @current-change="fetchList"
      />
    </div>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.editing ? '修改${options.functionName}' : '新增${options.functionName}'"
      width="560px"
    >
      <el-form label-width="110px">
<#list table.columns as col>
  <#if col.formField>
      <el-form-item label="${col.comment!col.javaName}">
    <#if col.javaType == "String" && (col.columnType!?lower_case)?contains("text")>
        <el-input v-model="form.${col.javaName}" type="textarea" :rows="3" />
    <#elseif col.javaType == "Integer" || col.javaType == "Long" || col.javaType == "Double" || col.javaType == "BigDecimal">
        <el-input-number v-model="form.${col.javaName}" />
    <#else>
        <el-input v-model="form.${col.javaName}" />
    </#if>
      </el-form-item>
  </#if>
</#list>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">確定</el-button>
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
.add-btn {
  margin-left: auto;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
