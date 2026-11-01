<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createMenu, deleteMenu, listMenus, updateMenu } from '@/api/system'
import type { MenuSaveBody } from '@/api/system'
import type { MenuType, MenuVO } from '@/api/types'
import { buildTree } from '@/utils/menu'
import { getIcon } from '@/utils/icons'
import IconPicker from '@/components/IconPicker.vue'

const { t } = useI18n()

const loading = ref(false)
const flatList = ref<MenuVO[]>([])
const keyword = ref('')

const menuTree = computed(() => buildTree(flatList.value))

const menuTypeMeta = computed<Record<MenuType, { label: string; tag: 'primary' | 'success' | 'info' }>>(() => ({
  D: { label: t('menu.typeDir'), tag: 'primary' },
  M: { label: t('menu.typeMenu'), tag: 'success' },
  B: { label: t('menu.typeButton'), tag: 'info' },
}))

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
    name: t('menu.rootMenu'),
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
    ElMessage.warning(t('menu.nameRequired'))
    return
  }
  if (form.menuType !== 'B' && !form.path.trim()) {
    ElMessage.warning(t('menu.pathRequired'))
    return
  }
  if (form.menuType === 'M' && !form.component.trim()) {
    ElMessage.warning(t('menu.componentRequired'))
    return
  }
  if (form.menuType === 'B' && !form.perm.trim()) {
    ElMessage.warning(t('menu.permRequired'))
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
    ElMessage.success(t('menu.updateSuccess'))
  } else {
    await createMenu(body)
    ElMessage.success(t('menu.createSuccess'))
  }
  dialogVisible.value = false
  void fetchList()
}

async function handleDelete(row: MenuVO): Promise<void> {
  try {
    await ElMessageBox.confirm(t('menu.deleteConfirm', { name: row.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteMenu(row.id)
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
        :placeholder="t('menu.searchPlaceholder')"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleReset"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">{{ t('common.search') }}</el-button>
      <el-button :icon="Refresh" @click="handleReset">{{ t('common.reset') }}</el-button>
      <el-button v-permission="'system:menu:add'" type="primary" :icon="Plus" @click="openCreate()">
        {{ t('menu.addMenu') }}
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="menuTree"
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column :label="t('menu.name')" min-width="180">
        <template #default="{ row }">
          <el-icon v-if="row.icon" class="menu-icon"><component :is="getIcon(row.icon)" /></el-icon>
          <span>{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('menu.type')" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="menuTypeMeta[row.menuType as MenuType].tag">
            {{ menuTypeMeta[row.menuType as MenuType].label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" :label="t('menu.path')" min-width="140" />
      <el-table-column prop="component" :label="t('menu.component')" min-width="180" />
      <el-table-column prop="perm" :label="t('menu.perm')" min-width="160" />
      <el-table-column prop="sortNo" :label="t('common.sortNo')" width="70" align="center" />
      <el-table-column :label="t('menu.visible')" width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.menuType !== 'B'" :type="row.visible === 0 ? 'info' : 'success'">
            {{ row.visible === 0 ? t('menu.hidden') : t('menu.shown') }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.status')" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? t('menu.normal') : t('common.disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="220" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.menuType !== 'B'"
            v-permission="'system:menu:add'"
            link
            type="primary"
            @click="openCreate(row as MenuVO)"
          >
            {{ t('menu.addChild') }}
          </el-button>
          <el-button v-permission="'system:menu:edit'" link type="primary" @click="openEdit(row as MenuVO)">
            {{ t('common.edit') }}
          </el-button>
          <el-button
            v-permission="'system:menu:delete'"
            link
            type="danger"
            @click="handleDelete(row as MenuVO)"
          >
            {{ t('common.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? t('menu.editMenu') : t('menu.addMenu')"
      width="560px"
      destroy-on-close
    >
      <el-form label-width="90px">
        <el-form-item :label="t('menu.parentMenu')">
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
        <el-form-item :label="t('menu.menuType')">
          <el-radio-group v-model="form.menuType">
            <el-radio-button value="D">{{ t('menu.typeDir') }}</el-radio-button>
            <el-radio-button value="M">{{ t('menu.typeMenu') }}</el-radio-button>
            <el-radio-button value="B">{{ t('menu.typeButton') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('menu.name')" required>
          <el-input v-model="form.name" :placeholder="t('menu.namePlaceholder')" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'B'" :label="t('menu.path')" required>
          <el-input v-model="form.path" :placeholder="t('menu.pathPlaceholder')" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 'M'" :label="t('menu.component')" required>
          <el-input v-model="form.component" :placeholder="t('menu.componentPlaceholder')" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'D'" :label="t('menu.perm')">
          <el-input v-model="form.perm" :placeholder="t('menu.permPlaceholder')" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'B'" :label="t('menu.icon')">
          <IconPicker v-model="form.icon" :placeholder="t('menu.iconPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('common.sortNo')">
          <el-input-number v-model="form.sortNo" :min="0" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'B'" :label="t('menu.visible')">
          <el-switch
            v-model="form.visible"
            :active-value="1"
            :inactive-value="0"
            :active-text="t('menu.shown')"
            :inactive-text="t('menu.hidden')"
          />
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            :active-text="t('menu.normal')"
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
.menu-icon {
  margin-right: 6px;
  vertical-align: -2px;
}
</style>
