# NovaBoot

一套開箱即用的開源企業級 Web 應用基礎框架：內置用戶、角色、選單（RBAC 權限體系）等後台系統標配能力，前後端分離，MIT 協議。

> 當前狀態：**M0 骨架**（登入/驗證碼、用戶/角色/選單 CRUD、動態選單路由、按鈕級權限）。
> 技術選型與總體方案見 [docs/01-技術選型與總體方案.md](docs/01-技術選型與總體方案.md)。

## 技術棧

| 層 | 選型 |
|---|---|
| 後端 | JDK 21+（推薦 25 LTS）· Spring Boot 4.1 · Sa-Token · MyBatis-Plus · Flyway · MySQL 8.4 / H2 |
| 前端 | Vue 3.5 · TypeScript · Vite · Element Plus · Pinia · UnoCSS（pnpm monorepo） |
| 工程 | Maven 多模組 · Spring Boot Starter 式模組邊界 · GitHub Actions（待接入） |

## 快速開始（後端）

```bash
# 默認 local profile：H2 內存庫，零外部依賴啟動
cd nova-server-admin
mvn -pl .. -am package -DskipTests   # 或直接在根目錄 mvn package -DskipTests
mvn spring-boot:run                  # 端口 8080
```

- 默認賬號：`admin`，初始密碼見 `application.yml` 的 `nova.init.admin-password`
- 介面文檔：<http://localhost:8080/swagger-ui.html>
- 切換 MySQL：`--spring.profiles.active=mysql`（連接參數見 `application-mysql.yml`，可通過 `MYSQL_*` 環境變量覆蓋；建表與種子數據由 Flyway 自動執行）

## 快速開始（前端）

```bash
cd web
pnpm install
pnpm dev     # 開發模式，/api 代理到 localhost:8080
pnpm build   # 產物在 web/apps/admin/dist
```

## 模組結構

```
nova-boot/
├─ nova-dependencies/       # 三方依賴版本 BOM
├─ nova-common/             # 統一響應、分頁、異常、錯誤碼
├─ nova-framework/          # Web / Sa-Token / MyBatis-Plus / 驗證碼 / 全局異常
├─ nova-module-system/      # 系統模組：用戶、角色、選單（RBAC）
├─ nova-server-admin/       # 後台服務啟動器（Flyway 腳本在此）
└─ web/                     # 前端 pnpm monorepo（apps/admin）
```

## 路線圖

- [x] M0 骨架：登入/驗證碼、用戶/角色/選單、動態路由、按鈕權限
- [ ] M1 系統模組：部門崗位、字典參數、日誌審計、資料權限、檔案上傳、Redis 會話
- [ ] M2 效率與交付：代碼生成器、Excel、監控、Docker 一鍵部署、文檔站、v1.0
- [ ] M3 生態：多租戶、工作流、AI Starter、插件機制

## 協議

MIT
