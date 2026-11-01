import { reactive, watchEffect } from 'vue'

export type LayoutMode = 'sidebar' | 'top'
export type AsideSkin = 'follow' | 'dark' | 'light'

export interface LayoutState {
  mode: LayoutMode
  asideSkin: AsideSkin
}

const KEY = 'nova_layout'

const allModes: LayoutMode[] = ['sidebar', 'top']
const allSkins: AsideSkin[] = ['follow', 'dark', 'light']

function readInitial(): LayoutState {
  try {
    const raw = localStorage.getItem(KEY)
    if (raw) {
      const parsed = JSON.parse(raw) as Partial<LayoutState>
      return {
        mode: parsed.mode && allModes.includes(parsed.mode) ? parsed.mode : 'sidebar',
        asideSkin: parsed.asideSkin && allSkins.includes(parsed.asideSkin) ? parsed.asideSkin : 'follow',
      }
    }
  } catch {
    // localStorage 不可用時使用默認值
  }
  return { mode: 'sidebar', asideSkin: 'follow' }
}

const state = reactive<LayoutState>(readInitial())

// 即時同步到 <html> data 屬性並持久化（與主題/語言同一機制）
watchEffect(() => {
  const root = document.documentElement
  root.dataset.layout = state.mode
  root.dataset.aside = state.asideSkin
  try {
    localStorage.setItem(KEY, JSON.stringify(state))
  } catch {
    // 忽略持久化失敗
  }
})

export function useLayout(): { layout: LayoutState } {
  return { layout: state }
}
