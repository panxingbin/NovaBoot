import { ref, watch } from 'vue'
import { Brush, MagicStick, Moon, Sunny } from '@element-plus/icons-vue'
import type { Component } from 'vue'

export type ThemeId = 'light' | 'dark' | 'glass' | 'manga'

export interface ThemeMeta {
  id: ThemeId
  label: string
  icon: Component
}

export const themes: ThemeMeta[] = [
  { id: 'light', label: '亮色', icon: Sunny },
  { id: 'dark', label: '暗色', icon: Moon },
  { id: 'glass', label: '液態玻璃', icon: MagicStick },
  { id: 'manga', label: '漫畫', icon: Brush },
]

const THEME_KEY = 'nova_theme'

function readInitial(): ThemeId {
  const all: ThemeId[] = ['light', 'dark', 'glass', 'manga']
  // URL 查詢參數優先（?theme=dark 等），便於深鏈接分享
  try {
    const fromQuery = new URLSearchParams(window.location.search).get('theme') as ThemeId | null
    if (fromQuery && all.includes(fromQuery)) {
      localStorage.setItem(THEME_KEY, fromQuery)
      return fromQuery
    }
    const stored = localStorage.getItem(THEME_KEY)
    if (stored && all.includes(stored as ThemeId)) {
      return stored as ThemeId
    }
  } catch {
    // localStorage 不可用時回退亮色
  }
  return 'light'
}

function apply(id: ThemeId): void {
  const root = document.documentElement
  root.dataset.theme = id
  // Element Plus 暗色變量掛在 html.dark 上
  root.classList.toggle('dark', id === 'dark')
}

const current = ref<ThemeId>(readInitial())

function setTheme(id: ThemeId): void {
  current.value = id
  try {
    localStorage.setItem(THEME_KEY, id)
  } catch {
    // 忽略持久化失敗
  }
}

// 主題變化時即時同步到 <html>（immediate 同時覆蓋模塊加載時的首次初始化）
watch(current, (id) => apply(id), { immediate: true })

/**
 * 應用啟動時顯式同步一次（保證 mount 前完成首屏主題設置）
 */
export function initTheme(): void {
  apply(current.value)
}

export function useTheme(): {
  theme: typeof current
  setTheme: (id: ThemeId) => void
  themes: ThemeMeta[]
} {
  return { theme: current, setTheme, themes }
}
