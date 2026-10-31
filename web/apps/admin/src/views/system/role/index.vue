<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, TreeInstance } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  assignRoleMenus,
  createRole,
  deleteRole,
  getRoleMenuIds,
  getMenuTree,
  pageRoles,
  updateRole,
} from '@/api/system'
import type { MenuTreeNode, RoleVO } from '@/api/types'
import { collectLeafIds } from '@/utils/menu'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const total = ref(0)
const records = ref<RoleVO[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const keyword = query.keyword.trim()
    const data = await pageRoles({
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

// ---------- 新增 / 修改 ----------

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(0)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  code: '',
  sortNo: 0,
  status: 1,
  remark: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '請輸入角色名稱', trigger: 'blur' }],
  code: [{ required: true, message: '請輸入角色編碼', trigger: 'blur' }],
}

function openCreate(): void {
  isEdit.value = false
  editingId.value = 0
  Object.assign(form, { name: '', code: '', sortNo: 0, status: 1, remark: '' })
  dialogVisible.value = true
}

function openEdit(row: RoleVO): void {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    code: row.code,
    sortNo: row.sortNo,
    status: row.status,
    remark: row.remark ?? '',
  })
  dialogVisible.value = true
}

async function handleSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  const payload = {
    name: form.name.trim(),
    code: form.code.trim(),
    sortNo: Number.isFinite(form.sortNo) ? form.sortNo : 0,
    status: form.status,
    remark: form.remark || undefined,
  }
  if (isEdit.value) {
    await updateRole(editingId.value, payload)
    ElMessage.success('修改成功')
  } else {
    await createRole(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  void fetchList()
}

// ---------- 狀態開關 ----------

async function handleStatusChange(row: RoleVO): Promise<void> {
  const original = row.status === 1 ? 0 : 1
  try {
    await updateRole(row.id, {
      name: row.name,
      code: row.code,
      sortNo: row.sortNo,
      status: row.status,
      remark: row.remark ?? undefined,
    })
    ElMessage.success(row.status === 1 ? '已啟用' : '已停用')
  } catch {
    row.status = original
  }
}

// ---------- 分配菜單權限 ----------

const assignDialogVisible = ref(false)
const assigningRole = ref<RoleVO | null>(null)
const assignLoading = ref(false)
const treeRef = ref<TreeInstance>()
const menuTreeData = ref<MenuTreeNode[]>([])

async function openAssign(row: RoleVO): Promise<void> {
  assigningRole.value = row
  assignDialogVisible.value = true
  assignLoading.value = true
  try {
    const [tree, menuIds] = await Promise.all([getMenuTree(), getRoleMenuIds(row.id)])
    menuTreeData.value = tree
    const leafIds = collectLeafIds(tree)
    const checkedLeafIds = menuIds.filter((id) => leafIds.has(id))
    await nextTick()
    treeRef.value?.setCheckedKeys(checkedLeafIds)
  } finally {
    assignLoading.value = false
  }
}

async function handleAssignSubmit(): Promise<void> {
  const tree = treeRef.value
  if (!tree || !assigningRole.value) {
    return
  }
  const menuIds = [
    ...(tree.getCheckedKeys() as number[]),
    ...(tree.getHalfCheckedKeys() as number[]),
  ]
  await assignRoleMenus(assigningRole.value.id, menuIds)
  ElMessage.success('菜單權限分配成功')
  assignDialogVisible.value = false
}

// ---------- 刪除 ----------

async function handleDelete(row: RoleVO): Promise<void> {
  try {
    await ElMessageBox.confirm(`確定要刪除角色「${row.name}」嗎？`, '警告', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteRole(row.id)
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
        v-model="query.keyword"
        class="search-input"
        placeholder="角色名稱 / 編碼"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <el-button v-permission="'system:role:add'" type="primary" :icon="Plus" @click="openCreate">
        新增角色
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="name" label="名稱" min-width="120" />
      <el-table-column prop="code" label="編碼" min-width="120" />
      <el-table-column prop="sortNo" label="排序" width="70" />
      <el-table-column label="狀態" width="90">
        <template #default="{ row }">
          <el-switch
            v-permission="'system:role:edit'"
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row as RoleVO)"
          />
        </template>
      </el-table-column>
      <el-table-column label="備註" min-width="140">
        <template #default="{ row }">{{ row.remark || '-' }}</template>
      </el-table-column>
      <el-table-column label="創建時間" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:role:edit'" link type="primary" @click="openEdit(row as RoleVO)">
            修改
          </el-button>
          <el-button
            v-permission="'system:role:assign'"
            link
            type="primary"
            @click="openAssign(row as RoleVO)"
          >
            分配菜單權限
          </el-button>
          <el-button
            v-permission="'system:role:delete'"
            link
            type="danger"
            @click="handleDelete(row as RoleVO)"
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

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改角色' : '新增角色'"
      width="480px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名稱" prop="name">
          <el-input v-model="form.name" placeholder="請輸入角色名稱" />
        </el-form-item>
        <el-form-item label="編碼" prop="code">
          <el-input v-model="form.code" placeholder="請輸入角色編碼" />
        </el-form-item>
        <el-form-item label="排序" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="0" />
        </el-form-item>
        <el-form-item label="狀態" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="備註" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="請輸入備註" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">確定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogVisible" title="分配菜單權限" width="480px" destroy-on-close>
      <div v-loading="assignLoading" class="menu-tree-box">
        <el-tree
          ref="treeRef"
          :data="menuTreeData"
          node-key="id"
          show-checkbox
          default-expand-all
          :props="{ label: 'name', children: 'children' }"
        />
      </div>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit">確定</el-button>
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
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.menu-tree-box {
  max-height: 420px;
  overflow: auto;
}
</style>
