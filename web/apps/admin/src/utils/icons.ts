import type { Component } from 'vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const iconMap = ElementPlusIconsVue as unknown as Record<string, Component>

export function getIcon(name?: string | null): Component {
  if (name && iconMap[name]) {
    return iconMap[name]
  }
  return iconMap.Menu
}
