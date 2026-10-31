<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createDept, deleteDept, listDepts, updateDept } from '@/api/system'
import type { DeptVO } from '@/api/types'
import { buildTree } from '@/utils/menu'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const flatList = ref<DeptVO[]>([])
const keyword = ref('')

const deptTree = computed(() => buildTree(flatList.value))

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const kw = keyword.value.trim()
    flatList.value = await listDepts(kw || undefined)
  } finally {
    loading.value = false
  }
}

function handleSearch(): void {
  void fetchList()
}

function handleReset(): void {
  keyword.value = ''
  void fetchList()
}

// ---------- 新增 / 修改 ----------

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(0)

const form = reactive({
  id: 0,
  parentId: 0,
  name: '',
  leader: '',
  phone: '',
  sortNo: 0,
  status: 1,
})

const parentOptions = computed(() => [
  {
    id: 0,
    parentId: -1,
    name: '根部門',
    sortNo: 0,
    children: deptTree.value,
  },
])

function openCreate(parent?: DeptVO): void {
  isEdit.value = false
  editingId.value = 0
  Object.assign(form, {
    id: 0,
    parentId: parent ? parent.id : 0,
    name: '',
    leader: '',
    phone: '',
    sortNo: 0,
    status: 1,
  })
  dialogVisible.value = true
}

function openEdit(row: DeptVO): void {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId,
    name: row.name,
    leader: row.leader ?? '',
    phone: row.phone ?? '',
    sortNo: row.sortNo,
    status: row.status,
  })
  dialogVisible.value = true
}

async function handleSubmit(): Promise<void> {
  if (!form.name.trim()) {
    ElMessage.warning('請輸入部門名稱')
    return
  }
  const body = {
    parentId: form.parentId,
    name: form.name.trim(),
    leader: form.leader.trim() || undefined,
    phone: form.phone.trim() || undefined,
    sortNo: Number.isFinite(form.sortNo) ? form.sortNo : 0,
    status: form.status,
  }
  if (isEdit.value) {
    await updateDept(editingId.value, body)
    ElMessage.success('修改成功')
  } else {
    await createDept(body)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  void fetchList()
}

async function handleDelete(row: DeptVO): Promise<void> {
  try {
    await ElMessageBox.confirm(`確定要刪除部門「${row.name}」嗎？`, '警告', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteDept(row.id)
  ElMessage.success('刪除成功')
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
        v-model="keyword"
        class="search-input"
        placeholder="部門名稱"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleReset"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <el-button v-permission="'system:dept:add'" type="primary" :icon="Plus" @click="openCreate()">
        新增部門
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="deptTree"
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="name" label="名稱" min-width="200" />
      <el-table-column label="負責人" min-width="110">
        <template #default="{ row }">{{ row.leader || '-' }}</template>
      </el-table-column>
      <el-table-column label="電話" min-width="130">
        <template #default="{ row }">{{ row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column prop="sortNo" label="排序" width="70" align="center" />
      <el-table-column label="狀態" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="創建時間" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button
            v-permission="'system:dept:add'"
            link
            type="primary"
            @click="openCreate(row as DeptVO)"
          >
            新增子部門
          </el-button>
          <el-button
            v-permission="'system:dept:edit'"
            link
            type="primary"
            @click="openEdit(row as DeptVO)"
          >
            修改
          </el-button>
          <el-button
            v-permission="'system:dept:delete'"
            link
            type="danger"
            @click="handleDelete(row as DeptVO)"
          >
            刪除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改部門' : '新增部門'"
      width="520px"
      destroy-on-close
    >
      <el-form label-width="90px">
        <el-form-item label="上級部門">
          <el-tree-select
            v-model="form.parentId"
            class="w-full"
            :data="parentOptions"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            check-strictly
            default-expand-all
            :render-after-expand="false"
          />
        </el-form-item>
        <el-form-item label="名稱" required>
          <el-input v-model="form.name" placeholder="請輸入部門名稱" />
        </el-form-item>
        <el-form-item label="負責人">
          <el-input v-model="form.leader" placeholder="請輸入負責人" />
        </el-form-item>
        <el-form-item label="電話">
          <el-input v-model="form.phone" placeholder="請輸入聯繫電話" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNo" :min="0" />
        </el-form-item>
        <el-form-item label="狀態">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="正常"
            inactive-text="停用"
          />
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
</style>
