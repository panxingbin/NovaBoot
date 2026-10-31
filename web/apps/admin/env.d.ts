/// <reference types="vite/client" />
/// <reference types="element-plus/global" />

export {}

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
  }
}
