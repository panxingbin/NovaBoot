# 主題切換

NovaBoot 管理端內置**四套主題**，基於 CSS 變量 + `data-theme` 屬性實現，切換即時生效、刷新不丟。

| 主題 | 標識 | 說明 |
|---|---|---|
| 亮色 | `light` | 默認主題 |
| 暗色 | `dark` | 同步在 `<html>` 上掛 `dark` 類，適配 Element Plus 暗色變量 |
| 液態玻璃 | `glass` | 毛玻璃質感 |
| 漫畫 | `manga` | 高對比描邊風格 |

## 切換入口

登入後點擊**頂欄右側的主題切換下拉**（圖標分別為 ☀️ / 🌙 / ✨ / 🖌️）即可即時切換，選擇持久化到 `localStorage`（鍵名 `nova_theme`）。

## 深鏈接

支持 URL 查詢參數直達主題，便於分享與截圖：

```text
http://localhost:5173/?theme=dark
http://localhost:5173/?theme=glass
```

`?theme=` 的優先級高於 `localStorage`，且會把該主題寫回本地持久化；取值必須是上表四個標識之一，非法值回退默認亮色。

## 實現機制

主題邏輯集中在 `web/apps/admin/src/theme/`：

- `useTheme.ts`：維護當前主題的響應式狀態，寫入 `document.documentElement.dataset.theme`（即 `<html data-theme="dark">`），`dark` 主題額外切換 `html.dark` 類；
- `theme.css`：為每套主題定義 `html[data-theme='light'] { … }` 等變量組，Element Plus 變量通過 `html.dark` 覆蓋。

新增一套主題只需三步：

1. 在 `useTheme.ts` 的 `themes` 數組與 `ThemeId` 中註冊新標識；
2. 在 `theme.css` 中添加對應 `html[data-theme='你的主題']` 變量組；
3. 頂欄主題下拉由 `themes` 數組驅動渲染（`v-for="t in themes"`），新主題會自動出現。
