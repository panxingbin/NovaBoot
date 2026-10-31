<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  createUser,
  deleteUser,
  pageRoles,
  pageUsers,
  updateUser,
  updateUserPassword,
} from '@/api/system'
import type { RoleVO, UserVO } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const total = ref(0)
const records = ref<UserVO[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const keyword = query.keyword.trim()
    const data = await pageUsers({
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
const roleOptions = ref<RoleVO[]>([])

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  deptId: undefined as number | undefined,
  email: '',
  phone: '',
  status: 1,
  remark: '',
  roleIds: [] as number[],
})

const rules = computed<FormRules>(() => ({
  username: [{ required: true, message: '請輸入用戶名', trigger: 'blur' }],
  password: isEdit.value ? [] : [{ required: true, message: '請輸入密碼', trigger: 'blur' }],
  nickname: [{ required: true, message: '請輸入暱稱', trigger: 'blur' }],
}))

async function fetchRoleOptions(): Promise<void> {
  const data = await pageRoles({ pageNum: 1, pageSize: 100 })
  roleOptions.value = data.records
}

function openCreate(): void {
  isEdit.value = false
  editingId.value = 0
  Object.assign(form, {
    username: '',
    password: '',
    nickname: '',
    deptId: undefined,
    email: '',
    phone: '',
    status: 1,
    remark: '',
    roleIds: [],
  })
  dialogVisible.value = true
}

function openEdit(row: UserVO): void {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    username: row.username,
    password: '',
    nickname: row.nickname,
    deptId: row.deptId ?? undefined,
    email: row.email ?? '',
    phone: row.phone ?? '',
    status: row.status,
    remark: row.remark ?? '',
    roleIds: [...row.roleIds],
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
    nickname: form.nickname,
    deptId: form.deptId,
    email: form.email || undefined,
    phone: form.phone || undefined,
    status: form.status,
    remark: form.remark || undefined,
    roleIds: form.roleIds,
  }
  if (isEdit.value) {
    await updateUser(editingId.value, payload)
    ElMessage.success('修改成功')
  } else {
    await createUser({ ...payload, username: form.username, password: form.password })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  void fetchList()
}

// ---------- 狀態開關 ----------

async function handleStatusChange(row: UserVO): Promise<void> {
  const original = row.status === 1 ? 0 : 1
  try {
    await updateUser(row.id, {
      nickname: row.nickname,
      deptId: row.deptId ?? undefined,
      email: row.email ?? undefined,
      phone: row.phone ?? undefined,
      status: row.status,
      remark: row.remark ?? undefined,
      roleIds: row.roleIds,
    })
    ElMessage.success(row.status === 1 ? '已啟用' : '已停用')
  } catch {
    row.status = original
  }
}

// ---------- 重置密碼 ----------

const pwdDialogVisible = ref(false)
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ userId: 0, username: '', password: '' })
const pwdRules: FormRules = {
  password: [
    { required: true, message: '請輸入新密碼', trigger: 'blur' },
    { min: 6, message: '密碼至少 6 位', trigger: 'blur' },
  ],
}

function openResetPwd(row: UserVO): void {
  pwdForm.userId = row.id
  pwdForm.username = row.username
  pwdForm.password = ''
  pwdDialogVisible.value = true
}

async function handleResetPwd(): Promise<void> {
  try {
    await pwdFormRef.value?.validate()
  } catch {
    return
  }
  await updateUserPassword(pwdForm.userId, pwdForm.password)
  ElMessage.success('密碼重置成功')
  pwdDialogVisible.value = false
}

// ---------- 刪除 ----------

async function handleDelete(row: UserVO): Promise<void> {
  try {
    await ElMessageBox.confirm(`確定要刪除用戶「${row.username}」嗎？`, '警告', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteUser(row.id)
  ElMessage.success('刪除成功')
  if (records.value.length === 1 && query.pageNum > 1) {
    query.pageNum -= 1
  }
  void fetchList()
}

onMounted(() => {
  void fetchList()
  void fetchRoleOptions()
})
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        class="search-input"
        placeholder="用戶名 / 暱稱"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <el-button v-permission="'system:user:add'" type="primary" :icon="Plus" @click="openCreate">
        新增用戶
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="username" label="用戶名" min-width="110" />
      <el-table-column prop="nickname" label="暱稱" min-width="110" />
      <el-table-column label="郵箱" min-width="160">
        <template #default="{ row }">{{ row.email || '-' }}</template>
      </el-table-column>
      <el-table-column label="手機" min-width="130">
        <template #default="{ row }">{{ row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column label="狀態" width="90">
        <template #default="{ row }">
          <el-switch
            v-permission="'system:user:edit'"
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row as UserVO)"
          />
        </template>
      </el-table-column>
      <el-table-column label="創建時間" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:user:edit'" link type="primary" @click="openEdit(row as UserVO)">
            修改
          </el-button>
          <el-button
            v-permission="'system:user:resetPwd'"
            link
            type="warning"
            @click="openResetPwd(row as UserVO)"
          >
            重置密碼
          </el-button>
          <el-button
            v-permission="'system:user:delete'"
            link
            type="danger"
            @click="handleDelete(row as UserVO)"
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
      :title="isEdit ? '修改用戶' : '新增用戶'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用戶名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="請輸入用戶名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密碼" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="請輸入密碼" />
        </el-form-item>
        <el-form-item label="暱稱" prop="nickname">
          <el-input v-model="form.nickname" placeholder="請輸入暱稱" />
        </el-form-item>
        <el-form-item label="郵箱" prop="email">
          <el-input v-model="form.email" placeholder="請輸入郵箱" />
        </el-form-item>
        <el-form-item label="手機" prop="phone">
          <el-input v-model="form.phone" placeholder="請輸入手機號" />
        </el-form-item>
        <el-form-item label="狀態" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" class="w-full" multiple placeholder="請選擇角色">
            <el-option v-for="role in roleOptions" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
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

    <el-dialog
      v-model="pwdDialogVisible"
      :title="`重置密碼 - ${pwdForm.username}`"
      width="420px"
      destroy-on-close
    >
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="新密碼" prop="password">
          <el-input
            v-model="pwdForm.password"
            type="password"
            show-password
            placeholder="請輸入新密碼"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPwd">確定</el-button>
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
</style>
