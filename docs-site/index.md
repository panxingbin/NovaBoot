---
layout: home

hero:
  name: NovaBoot
  text: 開箱即用的企業級 Web 應用基礎框架
  tagline: 模組化單體 · RBAC 權限 · 代碼生成 · 多主題 · 多語言 —— 前後端分離，把精力放在業務上
  actions:
    - theme: brand
      text: 快速開始
      link: /guide/quickstart
    - theme: alt
      text: Docker 一鍵部署
      link: /guide/deploy

features:
  - icon: 🧩
    title: 模組化單體
    details: Maven 多模組 + Starter 式平台能力（web / security / cache / storage / log），邊界清晰、可拆卸，模組邊界即未來的服務邊界。
  - icon: 🔐
    title: RBAC + 資料權限
    details: 用戶 / 角色 / 選單權限體系，接口級、選單級、按鈕級三層鑑權；角色綁定資料範圍，SQL 層自動注入過濾條件。
  - icon: ⚡
    title: 代碼生成器
    details: 讀取表元數據，在線選表 → 配置 → 預覽 → 下載 zip，一鍵生成前後端 CRUD 代碼與選單 SQL。
  - icon: 🎨
    title: 四套主題
    details: 亮色 / 暗色 / 液態玻璃 / 漫畫，基於 data-theme 的 CSS 變量切換，支持 ?theme= 深鏈接分享。
  - icon: 🌐
    title: 多語言
    details: 簡體中文 / 繁體中文 / English 三語言（vue-i18n），默認簡體中文，支持 ?lang= 深鏈接。
  - icon: 💾
    title: Redis 會話
    details: mysql profile 下會話 / 驗證碼 / 在線用戶存 Redis，支持強退與實時查詢；local 模式內存實現，零外部依賴啟動。
---
