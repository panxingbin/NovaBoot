<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createMenu, deleteMenu, listMenus, updateMenu } from '@/api/system'
import type { MenuSaveBody } from '@/api/system'
import type { MenuType, MenuVO } from '@/api/types'
import { buildTree } from '@/utils/menu'
import { getIcon } from '@/utils/icons'

const loading = ref(false)
const flatList = ref<MenuVO[]>([])
const keyword = ref('')

const menuTree = computed(() => buildTree(flatList.value))

const menuTypeMeta: Record<MenuType, { label: string; tag: 'primary' | 'success' | 'info' }> = {
  D: { label: '目錄', tag: 'primary' },
  M: { label: '菜單', tag: 'success' },
  B: { label: '按鈕', tag: 'info' },
}

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const kw = keyword.value.trim()
    flatList.value = await listMenus(kw || undefined)
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

const form = reactive({
  id: 0,
  parentId: 0,
  name: '',
  menuType: 'M' as MenuType,
  path: '',
  component: '',
  perm: '',
  icon: '',
  sortNo: 0,
  visible: 1,
  status: 1,
})

const parentOptions = computed(() => [
  {
    id: 0,
    parentId: -1,
    name: '根菜單',
    menuType: 'D' as MenuType,
    path: '',
    component: '',
    perm: '',
    icon: '',
    sortNo: 0,
    visible: 1,
    status: 1,
    children: menuTree.value,
  },
])

function openCreate(parent?: MenuVO): void {
  isEdit.value = false
  Object.assign(form, {
    id: 0,
    parentId: parent ? parent.id : 0,
    name: '',
    menuType: 'M' as MenuType,
    path: '',
    component: '',
    perm: '',
    icon: '',
    sortNo: 0,
    visible: 1,
    status: 1,
  })
  dialogVisible.value = true
}

function openEdit(row: MenuVO): void {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId,
    name: row.name,
    menuType: row.menuType,
    path: row.path ?? '',
    component: row.component ?? '',
    perm: row.perm ?? '',
    icon: row.icon ?? '',
    sortNo: row.sortNo,
    visible: row.visible,
    status: row.status,
  })
  dialogVisible.value = true
}

async function handleSubmit(): Promise<void> {
  if (!form.name.trim()) {
    ElMessage.warning('請輸入菜單名稱')
    return
  }
  if (form.menuType !== 'B' && !form.path.trim()) {
    ElMessage.warning('請輸入路由地址')
    return
  }
  if (form.menuType === 'M' && !form.component.trim()) {
    ElMessage.warning('請輸入組件路徑')
    return
  }
  if (form.menuType === 'B' && !form.perm.trim()) {
    ElMessage.warning('請輸入權限標識')
    return
  }
  const body: MenuSaveBody = {
    parentId: form.parentId,
    name: form.name.trim(),
    menuType: form.menuType,
    path: form.menuType === 'B' ? undefined : form.path.trim() || undefined,
    component: form.menuType === 'M' ? form.component.trim() || undefined : undefined,
    perm: form.perm.trim() || undefined,
    icon: form.icon.trim() || undefined,
    sortNo: Number.isFinite(form.sortNo) ? form.sortNo : 0,
    visible: form.visible,
    status: form.status,
  }
  if (isEdit.value) {
    await updateMenu(form.id, body)
    ElMessage.success('修改成功')
  } else {
    await createMenu(body)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  void fetchList()
}

async function handleDelete(row: MenuVO): Promise<void> {
  try {
    await ElMessageBox.confirm(`確定要刪除菜單「${row.name}」嗎？`, '警告', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteMenu(row.id)
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
        placeholder="菜單名稱 / 權限標識"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleReset"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <el-button v-permission="'system:menu:add'" type="primary" :icon="Plus" @click="openCreate()">
        新增菜單
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="menuTree"
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column label="名稱" min-width="180">
        <template #default="{ row }">
          <el-icon v-if="row.icon" class="menu-icon"><component :is="getIcon(row.icon)" /></el-icon>
          <span>{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="類型" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="menuTypeMeta[row.menuType as MenuType].tag">
            {{ menuTypeMeta[row.menuType as MenuType].label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路由地址" min-width="140" />
      <el-table-column prop="component" label="組件路徑" min-width="180" />
      <el-table-column prop="perm" label="權限標識" min-width="160" />
      <el-table-column prop="sortNo" label="排序" width="70" align="center" />
      <el-table-column label="可見" width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.menuType !== 'B'" :type="row.visible === 0 ? 'info' : 'success'">
            {{ row.visible === 0 ? '隱藏' : '顯示' }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="狀態" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.menuType !== 'B'"
            v-permission="'system:menu:add'"
            link
            type="primary"
            @click="openCreate(row as MenuVO)"
          >
            新增子項
          </el-button>
          <el-button v-permission="'system:menu:edit'" link type="primary" @click="openEdit(row as MenuVO)">
            修改
          </el-button>
          <el-button
            v-permission="'system:menu:delete'"
            link
            type="danger"
            @click="handleDelete(row as MenuVO)"
          >
            刪除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改菜單' : '新增菜單'"
      width="560px"
      destroy-on-close
    >
      <el-form label-width="90px">
        <el-form-item label="上級菜單">
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
        <el-form-item label="菜單類型">
          <el-radio-group v-model="form.menuType">
            <el-radio-button value="D">目錄</el-radio-button>
            <el-radio-button value="M">菜單</el-radio-button>
            <el-radio-button value="B">按鈕</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="名稱" required>
          <el-input v-model="form.name" placeholder="請輸入菜單名稱" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'B'" label="路由地址" required>
          <el-input v-model="form.path" placeholder="如 /system 或 /system/user" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 'M'" label="組件路徑" required>
          <el-input v-model="form.component" placeholder="如 system/user/index" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'D'" label="權限標識">
          <el-input v-model="form.perm" placeholder="如 system:user:add" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'B'" label="圖標">
          <div class="icon-row">
            <el-input v-model="form.icon" placeholder="Element Plus 圖標名，如 Setting" />
            <el-icon v-if="form.icon" class="icon-preview">
              <component :is="getIcon(form.icon)" />
            </el-icon>
          </div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNo" :min="0" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'B'" label="可見">
          <el-switch
            v-model="form.visible"
            :active-value="1"
            :inactive-value="0"
            active-text="顯示"
            inactive-text="隱藏"
          />
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
.menu-icon {
  margin-right: 6px;
  vertical-align: -2px;
}
.icon-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}
.icon-preview {
  font-size: 20px;
  color: var(--el-color-primary);
}
</style>
