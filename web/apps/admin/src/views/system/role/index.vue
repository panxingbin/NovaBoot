<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, TreeInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  assignRoleDepts,
  assignRoleMenus,
  createRole,
  deleteRole,
  getDeptTree,
  getRoleDeptIds,
  getRoleMenuIds,
  getMenuTree,
  pageRoles,
  updateRole,
} from '@/api/system'
import type { DeptVO, MenuTreeNode, RoleVO } from '@/api/types'
import { collectLeafIds } from '@/utils/menu'
import { formatDateTime } from '@/utils/format'

const { t } = useI18n()

const dataScopeOptions = computed<Array<{ value: number; label: string }>>(() => [
  { value: 1, label: t('role.dataScopeAll') },
  { value: 2, label: t('role.dataScopeCustom') },
  { value: 3, label: t('role.dataScopeDept') },
  { value: 4, label: t('role.dataScopeDeptBelow') },
  { value: 5, label: t('role.dataScopeSelf') },
])

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
  dataScope: 1,
  remark: '',
})

const rules: FormRules = {
  name: [{ required: true, message: () => t('role.nameRequired'), trigger: 'blur' }],
  code: [{ required: true, message: () => t('role.codeRequired'), trigger: 'blur' }],
}

function openCreate(): void {
  isEdit.value = false
  editingId.value = 0
  Object.assign(form, { name: '', code: '', sortNo: 0, status: 1, dataScope: 1, remark: '' })
  pendingDeptEcho.value = null
  void fetchDeptTree()
  dialogVisible.value = true
}

async function openEdit(row: RoleVO): Promise<void> {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    code: row.code,
    sortNo: row.sortNo,
    status: row.status,
    dataScope: row.dataScope,
    remark: row.remark ?? '',
  })
  dialogVisible.value = true
  try {
    const [tree, deptIds] = await Promise.all([getDeptTree(), getRoleDeptIds(row.id)])
    deptTreeData.value = tree
    const leafIds = collectLeafIds(tree)
    const checkedLeafIds = deptIds.filter((id) => leafIds.has(id))
    if (form.dataScope === 2) {
      await nextTick()
      deptTreeRef.value?.setCheckedKeys(checkedLeafIds)
      pendingDeptEcho.value = null
    } else {
      pendingDeptEcho.value = checkedLeafIds
    }
  } catch {
    // 部門回顯失敗不阻塞編輯，錯誤提示由 http 層統一彈出
  }
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
    dataScope: form.dataScope,
    remark: form.remark || undefined,
  }
  if (isEdit.value) {
    await updateRole(editingId.value, payload)
    if (payload.dataScope === 2) {
      await assignRoleDepts(editingId.value, collectDeptIds())
    }
    ElMessage.success(t('role.updateSuccess'))
  } else {
    const newId = await createRole(payload)
    if (payload.dataScope === 2) {
      if (typeof newId === 'number') {
        await assignRoleDepts(newId, collectDeptIds())
      } else {
        ElMessage.warning(t('role.createSuccessCustomDeptTip'))
      }
    }
    ElMessage.success(t('role.createSuccess'))
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
      dataScope: row.dataScope,
      remark: row.remark ?? undefined,
    })
    ElMessage.success(row.status === 1 ? t('common.enabled') : t('common.disabled'))
  } catch {
    row.status = original
  }
}

// ---------- 自訂部門 ----------

const deptTreeRef = ref<TreeInstance>()
const deptTreeData = ref<DeptVO[]>([])
const pendingDeptEcho = ref<number[] | null>(null)

async function fetchDeptTree(): Promise<void> {
  try {
    deptTreeData.value = await getDeptTree()
  } catch {
    // 錯誤提示由 http 層統一彈出
  }
}

watch(
  () => form.dataScope,
  async (val) => {
    if (val === 2 && pendingDeptEcho.value) {
      const ids = pendingDeptEcho.value
      pendingDeptEcho.value = null
      await nextTick()
      deptTreeRef.value?.setCheckedKeys(ids)
    }
  },
)

function collectDeptIds(): number[] {
  const tree = deptTreeRef.value
  if (!tree) {
    return []
  }
  return [...(tree.getCheckedKeys() as number[]), ...(tree.getHalfCheckedKeys() as number[])]
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
  ElMessage.success(t('role.assignSuccess'))
  assignDialogVisible.value = false
}

// ---------- 刪除 ----------

async function handleDelete(row: RoleVO): Promise<void> {
  try {
    await ElMessageBox.confirm(t('role.deleteConfirm', { name: row.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteRole(row.id)
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
        :placeholder="t('role.searchPlaceholder')"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">{{ t('common.search') }}</el-button>
      <el-button :icon="Refresh" @click="handleReset">{{ t('common.reset') }}</el-button>
      <el-button v-permission="'system:role:add'" type="primary" :icon="Plus" @click="openCreate">
        {{ t('role.addRole') }}
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="name" :label="t('role.name')" min-width="120" />
      <el-table-column prop="code" :label="t('role.code')" min-width="120" />
      <el-table-column prop="sortNo" :label="t('common.sortNo')" width="70" />
      <el-table-column :label="t('common.status')" width="90">
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
      <el-table-column :label="t('common.remark')" min-width="140">
        <template #default="{ row }">{{ row.remark || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('common.createdAt')" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="240" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:role:edit'" link type="primary" @click="openEdit(row as RoleVO)">
            {{ t('common.edit') }}
          </el-button>
          <el-button
            v-permission="'system:role:assign'"
            link
            type="primary"
            @click="openAssign(row as RoleVO)"
          >
            {{ t('role.assignMenus') }}
          </el-button>
          <el-button
            v-permission="'system:role:delete'"
            link
            type="danger"
            @click="handleDelete(row as RoleVO)"
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

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? t('role.editRole') : t('role.addRole')"
      width="480px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="t('role.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('role.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('role.code')" prop="code">
          <el-input v-model="form.code" :placeholder="t('role.codePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('common.sortNo')" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="0" />
        </el-form-item>
        <el-form-item :label="t('common.status')" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item :label="t('role.dataScope')">
          <el-select v-model="form.dataScope" class="w-full">
            <el-option
              v-for="opt in dataScopeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.dataScope === 2" :label="t('role.customDept')">
          <div class="menu-tree-box dept-tree-box">
            <el-tree
              ref="deptTreeRef"
              :data="deptTreeData"
              node-key="id"
              show-checkbox
              default-expand-all
              :props="{ label: 'name', children: 'children' }"
            />
          </div>
        </el-form-item>
        <el-form-item :label="t('common.remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" :placeholder="t('role.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogVisible" :title="t('role.assignMenus')" width="480px" destroy-on-close>
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
        <el-button @click="assignDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleAssignSubmit">{{ t('common.confirm') }}</el-button>
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
