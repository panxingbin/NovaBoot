<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { createDept, deleteDept, listDepts, updateDept } from '@/api/system'
import type { DeptVO } from '@/api/types'
import { buildTree } from '@/utils/menu'
import { formatDateTime } from '@/utils/format'

const { t } = useI18n()

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
    name: t('dept.rootDept'),
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
    ElMessage.warning(t('dept.nameRequired'))
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
    ElMessage.success(t('common.saveSuccess'))
  } else {
    await createDept(body)
    ElMessage.success(t('common.saveSuccess'))
  }
  dialogVisible.value = false
  void fetchList()
}

async function handleDelete(row: DeptVO): Promise<void> {
  try {
    await ElMessageBox.confirm(t('dept.deleteConfirm', { name: row.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteDept(row.id)
  ElMessage.success(t('common.deleteSuccess'))
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
        :placeholder="t('dept.searchPlaceholder')"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleReset"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">{{ t('common.search') }}</el-button>
      <el-button :icon="Refresh" @click="handleReset">{{ t('common.reset') }}</el-button>
      <el-button v-permission="'system:dept:add'" type="primary" :icon="Plus" @click="openCreate()">
        {{ t('dept.addDept') }}
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="deptTree"
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="name" :label="t('dept.name')" min-width="200" />
      <el-table-column :label="t('dept.leader')" min-width="110">
        <template #default="{ row }">{{ row.leader || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('dept.phone')" min-width="130">
        <template #default="{ row }">{{ row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column prop="sortNo" :label="t('common.sortNo')" width="70" align="center" />
      <el-table-column :label="t('common.status')" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? t('dept.statusNormal') : t('common.disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.createdAt')" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="230" fixed="right">
        <template #default="{ row }">
          <el-button
            v-permission="'system:dept:add'"
            link
            type="primary"
            @click="openCreate(row as DeptVO)"
          >
            {{ t('dept.addChildDept') }}
          </el-button>
          <el-button
            v-permission="'system:dept:edit'"
            link
            type="primary"
            @click="openEdit(row as DeptVO)"
          >
            {{ t('common.edit') }}
          </el-button>
          <el-button
            v-permission="'system:dept:delete'"
            link
            type="danger"
            @click="handleDelete(row as DeptVO)"
          >
            {{ t('common.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? t('dept.editDept') : t('dept.addDept')"
      width="520px"
      destroy-on-close
    >
      <el-form label-width="90px">
        <el-form-item :label="t('dept.parentDept')">
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
        <el-form-item :label="t('dept.name')" required>
          <el-input v-model="form.name" :placeholder="t('dept.nameRequired')" />
        </el-form-item>
        <el-form-item :label="t('dept.leader')">
          <el-input v-model="form.leader" :placeholder="t('dept.leaderPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('dept.phone')">
          <el-input v-model="form.phone" :placeholder="t('dept.phonePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('common.sortNo')">
          <el-input-number v-model="form.sortNo" :min="0" />
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            :active-text="t('dept.statusNormal')"
            :inactive-text="t('common.disabled')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ t('common.confirm') }}</el-button>
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
