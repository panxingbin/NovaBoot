<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown, Expand, Fold, SwitchButton, UserFilled } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useTheme, type ThemeId } from '@/theme/useTheme'
import LangSwitch from '@/components/LangSwitch.vue'
import MenuItems from './MenuItems.vue'
import { filterVisibleMenus } from '@/utils/menu'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()
const { theme, setTheme, themes } = useTheme()

const isCollapse = ref(false)
const visibleMenus = computed(() => filterVisibleMenus(userStore.menus))
const activeMenu = computed(() => route.path)
const displayName = computed(() => userStore.nickname || userStore.username || t('layout.defaultUser'))
const currentTheme = computed(() => themes.find((t) => t.id === theme.value) ?? themes[0])
const pageTitle = computed(() => {
  const titleKey = route.meta.titleKey as string | undefined
  if (titleKey) {
    return t(titleKey)
  }
  return (route.meta.title as string | undefined) ?? ''
})

async function handleCommand(command: string | number | object): Promise<void> {
  if (command !== 'logout') {
    return
  }
  try {
    await ElMessageBox.confirm(t('layout.logoutConfirm'), t('common.tip'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    })
  } catch {
    return
  }
  await userStore.logout()
  router.replace('/login')
}

function handleThemeCommand(command: string | number | object): void {
  setTheme(command as ThemeId)
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
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <LangSwitch />
          <el-dropdown trigger="click" @command="handleThemeCommand">
            <el-icon class="theme-btn" :size="18" :title="currentTheme.label">
              <component :is="currentTheme.icon" />
            </el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="t in themes"
                  :key="t.id"
                  :command="t.id"
                  :icon="t.icon"
                  :class="{ 'is-current': t.id === theme }"
                >
                  {{ t.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-entry">
              <el-icon><UserFilled /></el-icon>
              <span class="user-name">{{ displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" :icon="SwitchButton">
                  {{ t('layout.logout') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
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
  background-color: var(--el-bg-color);
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
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 18px;
}
.collapse-btn {
  cursor: pointer;
}
.theme-btn {
  cursor: pointer;
  color: var(--el-text-color-regular);
  outline: none;
}
.theme-btn:hover {
  color: var(--el-color-primary);
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
  background-color: var(--el-bg-color-page);
}
</style>
