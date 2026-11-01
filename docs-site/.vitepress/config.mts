import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  lang: 'zh-TW',
  title: 'NovaBoot',
  description:
    '開箱即用的開源企業級 Web 應用基礎框架：RBAC 權限體系、代碼生成器、四套主題、三語言，Spring Boot 4 + Vue 3 前後端分離。',
  // 文檔中的 http://localhost:* 為本地運行時服務地址（後端 / Swagger / 開發服務器），非站內鏈接
  ignoreDeadLinks: [/^https?:\/\/localhost/],
  themeConfig: {
    nav: [
      { text: '首頁', link: '/' },
      { text: '指南', link: '/guide/quickstart' },
      { text: '部署', link: '/guide/deploy' },
    ],
    sidebar: [
      {
        text: '指南',
        items: [
          { text: '快速開始', link: '/guide/quickstart' },
          { text: '主題切換', link: '/guide/themes' },
          { text: '多語言', link: '/guide/i18n' },
          { text: '代碼生成', link: '/guide/generator' },
          { text: 'Docker 部署', link: '/guide/deploy' },
          { text: 'AI 與郵件 Starter', link: '/guide/starters' },
        ],
      },
    ],
    outline: { level: [2, 3], label: '本頁目錄' },
    docFooter: { prev: '上一頁', next: '下一頁' },
    lastUpdated: { text: '最後更新' },
    returnToTopLabel: '回到頂部',
    sidebarMenuLabel: '目錄',
    lightModeSwitchTitle: '切換到亮色',
    darkModeSwitchTitle: '切換到暗色',
    footer: {
      message: '基於 MIT 協議開源',
      copyright: 'Copyright © 2026 NovaBoot',
    },
  },
})
