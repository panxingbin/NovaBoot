<script setup lang="ts">
import { computed } from 'vue'
import { i18n, langs, setLang, type Lang } from '@/locales'

const current = computed(() => langs.find((l) => l.id === i18n.global.locale.value) ?? langs[0])

function handleCommand(command: string | number | object): void {
  setLang(command as Lang)
}
</script>

<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <span class="lang-btn" title="Language">{{ current.short }}</span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="l in langs"
          :key="l.id"
          :command="l.id"
          :class="{ 'is-current': l.id === i18n.global.locale.value }"
        >
          {{ l.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped>
.lang-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 6px;
  color: var(--el-text-color-regular);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid var(--el-border-color-light);
  border-radius: 14px;
  outline: none;
}
.lang-btn:hover {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-5);
}
</style>
