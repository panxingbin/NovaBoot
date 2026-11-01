# 代碼生成

NovaBoot 內置代碼生成器（`nova-module-generator`）：讀取 `information_schema` 表元數據，在線配置後一鍵生成**前後端 CRUD 代碼 + 選單 SQL**，支持在線預覽與 zip 下載。

## 使用流程

登入管理端進入「系統工具 → 代碼生成」（選單路徑 `/tool/gen`，接口均在 `/api/gen` 下，需 `tool:gen:list` / `tool:gen:preview` / `tool:gen:download` 權限）：

1. **選表**：`GET /api/gen/tables` 列出當前庫中可生成的業務表（排除系統表）；
2. **配置選項**：`GET /api/gen/meta/{tableName}` 查看表/列元數據，填寫生成選項；
3. **預覽**：`POST /api/gen/preview` 返回 `{ 文件相對路徑: 內容 }`，逐文件在線查看；
4. **下載 zip**：`POST /api/gen/download` 打包下載全部生成物。

## 生成選項

| 選項 | 說明 | 默認值 |
|---|---|---|
| `tableName` | 來源業務表名（如 `biz_order`） | 必填 |
| `moduleName` | 模塊名，影響包名與路由前綴（如 `order`） | 必填 |
| `businessName` | 業務名，影響類名 / 文件名 / 接口路徑（如 `order`） | 必填 |
| `functionName` | 功能顯示名（如 `訂單`），用於選單與頁面標題 | 必填 |
| `author` | 作者註釋 | `novaboot` |
| `menuStartId` | 選單 SQL 的目錄起始 ID | `1000` |

## 生成物與 zip 結構

```text
<businessName>-gen/
├─ backend/src/main/java/<packageName>/entity/<ClassName>.java
├─ backend/src/main/java/<packageName>/mapper/<ClassName>Mapper.java
├─ backend/src/main/java/<packageName>/service/<ClassName>Service.java
├─ backend/src/main/java/<packageName>/controller/<ClassName>Controller.java
├─ frontend/src/api/<businessName>.ts
├─ frontend/src/views/<moduleName>/<businessName>/index.vue
└─ sql/menu.sql
```

模板為 FreeMarker，位於 `nova-module-generator/src/main/resources/gen/templates/`（`entity.java.ftl` 等 7 個）。

## 生成代碼的接入步驟

zip 只是代碼的起點，拿到後需要手動接入：

1. **後端**：把 `backend/` 下的四個 Java 文件按包路徑複製到對應模塊（如新建 `nova-module-order`，或先放入 `nova-module-system`）。
2. **Mapper 掃描**：啟動類 `NovaApplication` 目前僅聲明 `@MapperScan("com.novaboot.module.system.mapper")`，**必須把生成代碼的 mapper 包加入掃描**，例如：

   ```java
   @MapperScan({"com.novaboot.module.system.mapper", "com.novaboot.module.order.mapper"})
   ```

   否則啟動後注入 `*Mapper` 會直接失敗。
3. **前端**：把 `frontend/src/api/<businessName>.ts` 複製到 `web/apps/admin/src/api/`，`frontend/src/views/<moduleName>/<businessName>/index.vue` 複製到 `web/apps/admin/src/views/` 下（動態路由按選單裡的 `component` 路徑加載頁面）。
4. **選單 SQL**：在 MySQL 中執行 `sql/menu.sql`。它會向 `sys_menu` 插入 1 個目錄 + 1 個選單 + 3 個按鈕（新增 / 修改 / 刪除權限字元），並向 `sys_role_menu` 授權給超級管理員角色（`role_id = 1`）。執行前請確認 `menuStartId` 起始的 ID 未被佔用。
5. 重啟後端、刷新前端：超級管理員即可在選單中看到新功能頁面。

::: warning
選單 SQL 直接寫庫，繞過了 Flyway 的版本化管理——這是有意的設計（生成代碼屬於業務側），但請把該 SQL 一併納入你自己的遷移腳本或變更記錄，避免各環境選單不一致。
:::
