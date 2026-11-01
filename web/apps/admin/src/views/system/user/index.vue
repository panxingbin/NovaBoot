<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Plus, Refresh, Search, Upload, Download } from '@element-plus/icons-vue'
import {
  createUser,
  deleteUser,
  exportUsers,
  getDeptTree,
  importUsers,
  listAllPosts,
  pageRoles,
  pageUsers,
  updateUser,
  updateUserPassword,
} from '@/api/system'
import type { DeptVO, PostVO, RoleVO, UserVO } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const { t } = useI18n()

const loading = ref(false)
const total = ref(0)
const records = ref<UserVO[]>([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  deptId: undefined as number | undefined,
})

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const keyword = query.keyword.trim()
    const data = await pageUsers({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: keyword || undefined,
      deptId: query.deptId,
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
  query.deptId = undefined
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
  postIds: [] as number[],
})

const rules = computed<FormRules>(() => ({
  username: [{ required: true, message: () => t('user.usernameRequired'), trigger: 'blur' }],
  password: isEdit.value ? [] : [{ required: true, message: () => t('user.passwordRequired'), trigger: 'blur' }],
  nickname: [{ required: true, message: () => t('user.nicknameRequired'), trigger: 'blur' }],
}))

async function fetchRoleOptions(): Promise<void> {
  const data = await pageRoles({ pageNum: 1, pageSize: 100 })
  roleOptions.value = data.records
}

const deptTreeData = ref<DeptVO[]>([])
const postOptions = ref<PostVO[]>([])

async function fetchDeptTree(): Promise<void> {
  try {
    deptTreeData.value = await getDeptTree()
  } catch {
    // 錯誤提示由 http 層統一彈出
  }
}

async function fetchPostOptions(): Promise<void> {
  try {
    postOptions.value = await listAllPosts()
  } catch {
    // 錯誤提示由 http 層統一彈出
  }
}

function handleDeptQueryChange(value: unknown): void {
  query.deptId = typeof value === 'number' ? value : undefined
  query.pageNum = 1
  void fetchList()
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
    postIds: [],
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
    postIds: [...row.postIds],
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
    postIds: form.postIds,
  }
  if (isEdit.value) {
    await updateUser(editingId.value, payload)
    ElMessage.success(t('user.updateSuccess'))
  } else {
    await createUser({ ...payload, username: form.username, password: form.password })
    ElMessage.success(t('user.createSuccess'))
  }
  dialogVisible.value = false
  void fetchList()
}

// ---------- Excel 導出 / 導入 ----------

async function handleExport(): Promise<void> {
  try {
    await exportUsers({
      keyword: query.keyword.trim() || undefined,
      deptId: query.deptId,
    })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('user.export'))
  }
}

async function handleImport(options: { file: File }): Promise<void> {
  try {
    const result = await importUsers(options.file)
    if (result.failureCount > 0) {
      const lines = [t('user.importFailedRows', { count: result.failureCount }), ...result.errors]
      await ElMessageBox.alert(lines.join('<br/>'), t('user.importResult'), {
        dangerouslyUseHTMLString: true,
        type: 'warning',
      })
    } else {
      ElMessage.success(t('user.importSuccess', { count: result.successCount }))
    }
    void fetchList()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('user.importLabel'))
  }
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
      postIds: row.postIds,
    })
    ElMessage.success(row.status === 1 ? t('common.enabled') : t('common.disabled'))
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
    { required: true, message: () => t('user.newPasswordRequired'), trigger: 'blur' },
    { min: 6, message: () => t('user.passwordMinLength'), trigger: 'blur' },
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
  ElMessage.success(t('user.resetPwdSuccess'))
  pwdDialogVisible.value = false
}

// ---------- 刪除 ----------

async function handleDelete(row: UserVO): Promise<void> {
  try {
    await ElMessageBox.confirm(t('user.deleteConfirm', { name: row.username }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteUser(row.id)
  ElMessage.success(t('common.deleteSuccess'))
  if (records.value.length === 1 && query.pageNum > 1) {
    query.pageNum -= 1
  }
  void fetchList()
}

onMounted(() => {
  void fetchList()
  void fetchRoleOptions()
  void fetchDeptTree()
  void fetchPostOptions()
})
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        class="search-input"
        :placeholder="t('user.searchPlaceholder')"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-tree-select
        v-model="query.deptId"
        class="dept-select"
        :data="deptTreeData"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        check-strictly
        clearable
        default-expand-all
        :render-after-expand="false"
        :placeholder="t('user.dept')"
        @change="handleDeptQueryChange"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">{{ t('common.search') }}</el-button>
      <el-button :icon="Refresh" @click="handleReset">{{ t('common.reset') }}</el-button>
      <el-button v-permission="'system:user:add'" type="primary" :icon="Plus" @click="openCreate">
        {{ t('user.addUser') }}
      </el-button>
      <el-upload
        class="import-uploader"
        :show-file-list="false"
        accept=".xlsx,.xls"
        :http-request="handleImport"
      >
        <el-button v-permission="'system:user:import'" type="warning" :icon="Upload">
          {{ t('user.importLabel') }}
        </el-button>
      </el-upload>
      <el-button v-permission="'system:user:export'" type="success" :icon="Download" @click="handleExport">
        {{ t('user.export') }}
      </el-button>
    </div>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="username" :label="t('user.username')" min-width="110" />
      <el-table-column prop="nickname" :label="t('user.nickname')" min-width="110" />
      <el-table-column :label="t('user.email')" min-width="160">
        <template #default="{ row }">{{ row.email || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('user.phone')" min-width="130">
        <template #default="{ row }">{{ row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('common.status')" width="90">
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
      <el-table-column :label="t('common.createdAt')" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:user:edit'" link type="primary" @click="openEdit(row as UserVO)">
            {{ t('common.edit') }}
          </el-button>
          <el-button
            v-permission="'system:user:resetPwd'"
            link
            type="warning"
            @click="openResetPwd(row as UserVO)"
          >
            {{ t('user.resetPwd') }}
          </el-button>
          <el-button
            v-permission="'system:user:delete'"
            link
            type="danger"
            @click="handleDelete(row as UserVO)"
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
      :title="isEdit ? t('user.editUser') : t('user.addUser')"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="t('user.username')" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" :placeholder="t('user.usernamePlaceholder')" />
        </el-form-item>
        <el-form-item v-if="!isEdit" :label="t('user.password')" prop="password">
          <el-input v-model="form.password" type="password" show-password :placeholder="t('user.passwordPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('user.nickname')" prop="nickname">
          <el-input v-model="form.nickname" :placeholder="t('user.nicknamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('user.email')" prop="email">
          <el-input v-model="form.email" :placeholder="t('user.emailPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('user.phone')" prop="phone">
          <el-input v-model="form.phone" :placeholder="t('user.phonePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('user.deptLabel')" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            class="w-full"
            :data="deptTreeData"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            check-strictly
            clearable
            default-expand-all
            :render-after-expand="false"
            :placeholder="t('user.deptPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('user.post')" prop="postIds">
          <el-select v-model="form.postIds" class="w-full" multiple :placeholder="t('user.postPlaceholder')">
            <el-option v-for="post in postOptions" :key="post.id" :label="post.name" :value="post.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.status')" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item :label="t('user.role')" prop="roleIds">
          <el-select v-model="form.roleIds" class="w-full" multiple :placeholder="t('user.rolePlaceholder')">
            <el-option v-for="role in roleOptions" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" :placeholder="t('user.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="pwdDialogVisible"
      :title="t('user.resetPwdTitle', { name: pwdForm.username })"
      width="420px"
      destroy-on-close
    >
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item :label="t('user.newPassword')" prop="password">
          <el-input
            v-model="pwdForm.password"
            type="password"
            show-password
            :placeholder="t('user.newPasswordPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleResetPwd">{{ t('common.confirm') }}</el-button>
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
.dept-select {
  width: 200px;
}
.import-uploader {
  display: inline-block;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
