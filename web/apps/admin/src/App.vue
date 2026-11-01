<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import zhTw from 'element-plus/es/locale/lang/zh-tw'
import en from 'element-plus/es/locale/lang/en'
import { i18n, type Lang } from '@/locales'
import { pageTitleOf } from '@/router'

const router = useRouter()

const epLocale = computed(() => {
  const lang = i18n.global.locale.value as Lang
  return { 'zh-CN': zhCn, 'zh-TW': zhTw, en }[lang]
})

// 切換語言時刷新瀏覽器標籤標題（路由不變，afterEach 不會觸發）
watch(
  () => i18n.global.locale.value,
  () => {
    const title = pageTitleOf(router.currentRoute.value)
    document.title = title ? `${title} · NovaBoot` : i18n.global.t('router.defaultTitle')
  },
)
</script>

<template>
  <el-config-provider :locale="epLocale">
    <router-view />
  </el-config-provider>
</template>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
}

body {
  font-family:
    'Helvetica Neue', Helvetica, 'PingFang TC', 'Microsoft JhengHei', 'Noto Sans TC', Arial,
    sans-serif;
  -webkit-font-smoothing: antialiased;
  background-color: var(--el-bg-color-page);
}
</style>
