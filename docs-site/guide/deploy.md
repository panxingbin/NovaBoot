# Docker 部署

NovaBoot 提供 Docker Compose 一鍵部署：**MySQL 8.4 + Redis 8 + 後端（Spring Boot 單體）+ 前端（nginx）**，全部憑證來自 `.env`，不在配置文件中寫死任何密碼。

## 部署步驟

```bash
# 1. 準備環境變量（倉庫根目錄執行）
cp .env.example .env
#    編輯 .env，修改 MYSQL_ROOT_PASSWORD 為強密碼
#    注意：.env 含真實密碼，請先將 .env 加入 .gitignore 再提交代碼

# 2. 構建並啟動
docker compose up -d --build
```

首次構建需要拉取基礎鏡像與 Maven/pnpm 依賴，耐心等待。啟動完成後：

| 入口 | 地址 | 說明 |
|---|---|---|
| 前端管理端 | <http://localhost> | nginx（80 端口），SPA + `/api` 反向代理 |
| 後端 API | <http://localhost:8080> | 直連後端容器 |
| 接口文檔 | <http://localhost:8080/swagger-ui.html> | SpringDoc / Swagger UI |

默認賬號 `admin`，初始密碼 `admin123`（`application.yml` 的 `nova.init.admin-password`，登入後請立即修改）。

## 啟動了什麼

| 服務 | 鏡像 | 說明 |
|---|---|---|
| mysql | `mysql:8.4` | 數據落具名卷 `mysql-data`；帶 `mysqladmin ping` 健康檢查 |
| redis | `redis:8-alpine` | 會話 / 驗證碼 / 在線用戶；帶 `redis-cli ping` 健康檢查 |
| backend | 根目錄 `Dockerfile` 構建 | 以 `mysql` profile 運行，依賴 mysql/redis 健康後啟動 |
| frontend | `web/Dockerfile` 構建 | nginx 託管 `web/apps/admin/dist`，`/api/` 反代到 `backend:8080` |

**數據初始化**：後端首次啟動時，Flyway 在 `MYSQL_DB`（默認 `nova_boot`）庫中自動執行版本化遷移（建表 + 種子數據，含管理員賬號與選單），無需手工執行 SQL。重啟容器數據不丟；`docker compose down -v` 才會清空。

## 環境變量（.env）

| 變量 | 默認值 | 說明 |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | 無（必填） | MySQL root 密碼；同時作為後端數據源密碼注入 |
| `MYSQL_DB` | `nova_boot` | 業務庫名，容器啟動時自動創建 |
| `REDIS_HOST` | `redis` | 後端連接的 Redis 主機（compose 服務名） |
| `REDIS_PORT` | `6379` | Redis 端口 |

後端數據源 / Redis 通過環境變量注入（`MYSQL_HOST` / `MYSQL_DB` / `MYSQL_PASSWORD` / `REDIS_HOST` 等，見 `application-mysql.example.yml` 的佔位符），由 `docker-compose.yml` 從 `.env` 插值。

## 監控端點

後端暴露 Actuator 端點（`management.endpoints.web.exposure.include: health,info,prometheus`），登入攔截策略不同：

- `GET /actuator/health` —— **公開**，無需登入，用於容器探活與負載均衡健康檢查；
- `GET /actuator/prometheus` —— **需要登入 Token**（Sa-Token 攔截器覆蓋 `/actuator/**`，僅豁免 `/actuator/health`）。抓取請求需攜帶：

  ```bash
  curl -H "Authorization: Bearer <登入返回的Token>" http://localhost:8080/actuator/prometheus
  ```

  即 Prometheus 抓取任務需配置對應的請求頭；Token 有效期見 `application.yml` 的 `sa-token.timeout`（默認 30 天），到期需更新。生產環境建議為監控單獨建立賬號或通過內網隔離。

## 常用運維命令

```bash
docker compose logs -f backend     # 跟蹤後端日誌
docker compose ps                  # 查看各服務健康狀態
docker compose down                # 停止（保留數據卷）
docker compose down -v             # 停止並清空數據庫卷（危險：數據將重置）
docker compose up -d --build       # 改代碼後重新構建啟動
```

::: tip 建議補充 .dockerignore
根目錄與 `web/` 建議添加 `.dockerignore`（忽略 `node_modules/`、`target/`、`.git/`、`.m2repo/`、`.pnpm-store/`、`application-mysql.yml` 等），可顯著縮小構建上下文，並避免本機的 `application-mysql.yml`（可能含內網連接信息）被烤入後端鏡像。`web/Dockerfile` 已對主機 `node_modules` 做了隔離處理，但上下文體積仍建議用 `.dockerignore` 優化。
:::
