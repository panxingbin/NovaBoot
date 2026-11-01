<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const { t } = useI18n()
const displayName = computed(() => userStore.nickname || userStore.username || t('layout.defaultUser'))
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <span class="card-title">{{ t('dashboard.title') }}</span>
    </template>
    <div class="welcome">
      {{ t('dashboard.welcome', { name: displayName }) }}
    </div>
    <div class="roles">
      <span class="roles-label">{{ t('dashboard.rolesLabel') }}</span>
      <el-tag v-for="role in userStore.roles" :key="role" class="role-tag">{{ role }}</el-tag>
      <span v-if="userStore.roles.length === 0" class="no-role">{{ t('dashboard.noRole') }}</span>
    </div>
  </el-card>
</template>

<style scoped>
.card-title {
  font-weight: 600;
}
.welcome {
  color: var(--el-text-color-primary);
  font-size: 16px;
}
.roles {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
}
.roles-label {
  color: var(--el-text-color-secondary);
}
.no-role {
  color: var(--el-text-color-placeholder);
  font-size: 13px;
}
</style>
