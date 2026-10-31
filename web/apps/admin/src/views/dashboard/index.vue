<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const displayName = computed(() => userStore.nickname || userStore.username || '用戶')
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <span class="card-title">首頁</span>
    </template>
    <div class="welcome">
      你好，<strong>{{ displayName }}</strong>，歡迎使用 NovaBoot 管理後台！
    </div>
    <div class="roles">
      <span class="roles-label">當前角色：</span>
      <el-tag v-for="role in userStore.roles" :key="role" class="role-tag">{{ role }}</el-tag>
      <span v-if="userStore.roles.length === 0" class="no-role">暫無角色</span>
    </div>
  </el-card>
</template>

<style scoped>
.card-title {
  font-weight: 600;
}
.welcome {
  color: #303133;
  font-size: 16px;
}
.roles {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
}
.roles-label {
  color: #909399;
}
.no-role {
  color: #c0c4cc;
  font-size: 13px;
}
</style>
