<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown, Expand, Fold, SwitchButton, UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import MenuItems from './MenuItems.vue'
import { filterVisibleMenus } from '@/utils/menu'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const visibleMenus = computed(() => filterVisibleMenus(userStore.menus))
const activeMenu = computed(() => route.path)
const displayName = computed(() => userStore.nickname || userStore.username || '用戶')

async function handleCommand(command: string | number | object): Promise<void> {
  if (command !== 'logout') {
    return
  }
  try {
    await ElMessageBox.confirm('確定要退出登錄嗎？', '提示', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await userStore.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container class="layout-root">
    <el-aside class="layout-aside" :width="isCollapse ? '64px' : '210px'">
      <div class="layout-logo">
        <span v-if="!isCollapse">NovaBoot</span>
        <span v-else>NB</span>
      </div>
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          class="sidebar-menu"
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
        >
          <MenuItems :nodes="visibleMenus" />
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="layout-body">
      <el-header class="layout-header" height="56px">
        <div class="header-left">
          <el-icon class="collapse-btn" :size="18" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span class="page-title">{{ route.meta.title ?? '' }}</span>
        </div>
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-entry">
            <el-icon><UserFilled /></el-icon>
            <span class="user-name">{{ displayName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout" :icon="SwitchButton">退出登錄</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-root {
  height: 100%;
}
.layout-aside {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-right: 1px solid var(--el-border-color-light);
  background-color: #fff;
  transition: width 0.2s;
}
.layout-logo {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  height: 56px;
  color: var(--el-color-primary);
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
}
.menu-scrollbar {
  flex: 1;
}
.sidebar-menu {
  border-right: none;
}
.layout-body {
  height: 100%;
}
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid var(--el-border-color-light);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.collapse-btn {
  cursor: pointer;
}
.page-title {
  font-size: 15px;
  font-weight: 600;
}
.user-entry {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  outline: none;
}
.user-name {
  font-size: 14px;
}
.layout-main {
  padding: 16px;
  background-color: #f5f7fa;
}
</style>
