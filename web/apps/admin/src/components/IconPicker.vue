<script setup lang="ts">
import { computed, ref } from 'vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { getIcon } from '@/utils/icons'

const props = defineProps<{
  modelValue?: string | null
  placeholder?: string
}>()

const emit = defineEmits<{ (e: 'update:modelValue', value: string): void }>()

const { t } = useI18n()

const visible = ref(false)
const keyword = ref('')

const allNames = (Object.keys(ElementPlusIconsVue) as string[]).sort()
const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return kw ? allNames.filter((name) => name.toLowerCase().includes(kw)) : allNames
})

function pick(name: string): void {
  emit('update:modelValue', name)
  visible.value = false
}

function clear(): void {
  emit('update:modelValue', '')
  visible.value = false
}
</script>

<template>
  <el-popover v-model:visible="visible" trigger="click" :width="380" popper-class="icon-picker-popover">
    <template #reference>
      <el-input
        :model-value="modelValue || ''"
        readonly
        class="icon-picker-input"
        :placeholder="placeholder || t('common.iconPicker')"
      >
        <template #prefix>
          <el-icon v-if="modelValue"><component :is="getIcon(modelValue)" /></el-icon>
        </template>
      </el-input>
    </template>

    <div class="picker-head">
      <el-input
        v-model="keyword"
        size="small"
        clearable
        :placeholder="t('common.iconSearch')"
        :prefix-icon="Search"
      />
      <el-button v-if="modelValue" size="small" text type="danger" @click="clear">
        {{ t('common.iconClear') }}
      </el-button>
    </div>
    <div class="icon-grid">
      <button
        v-for="name in filtered"
        :key="name"
        type="button"
        class="icon-cell"
        :class="{ active: name === modelValue }"
        :title="name"
        @click="pick(name)"
      >
        <el-icon :size="18"><component :is="getIcon(name)" /></el-icon>
        <span class="icon-name">{{ name }}</span>
      </button>
      <div v-if="filtered.length === 0" class="icon-empty">{{ t('common.iconEmpty') }}</div>
    </div>
  </el-popover>
</template>

<style scoped>
.icon-picker-input {
  width: 100%;
  cursor: pointer;
}
.icon-picker-input :deep(input) {
  cursor: pointer;
}
.picker-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.picker-head .el-input {
  flex: 1;
}
.icon-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  max-height: 300px;
  overflow-y: auto;
}
.icon-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 2px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
}
.icon-cell:hover {
  background: var(--el-fill-color-light);
}
.icon-cell.active {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}
.icon-name {
  max-width: 84px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11px;
  color: var(--el-text-color-secondary);
}
.icon-cell.active .icon-name {
  color: var(--el-color-primary);
}
.icon-empty {
  grid-column: 1 / -1;
  padding: 20px 0;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
