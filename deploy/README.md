# Docker 部署

NovaBoot 提供一鍵 Docker Compose 部署：MySQL 8.4 + Redis 8 + 後端（Spring Boot 單體）+ 前端（nginx 託管 SPA 並反代 `/api`）。

## 快速開始

```bash
# 1. 準備環境變量（含 MySQL root 密碼，切勿提交到倉庫；建議先把 .env 加入 .gitignore）
cp .env.example .env          # 修改 MYSQL_ROOT_PASSWORD 為你的強密碼

# 2. 構建並啟動
docker compose up -d --build
```

啟動完成後：

| 入口 | 地址 | 說明 |
|---|---|---|
| 前端 | <http://localhost> | 管理端（nginx，80 端口） |
| 後端 API | <http://localhost:8080> | 直連後端（8080 端口） |
| 接口文檔 | <http://localhost:8080/swagger-ui.html> | SpringDoc / Swagger UI |

默認賬號 `admin`，初始密碼見 `nova-server-admin/src/main/resources/application.yml` 的 `nova.init.admin-password`（`admin123`，登入後請立即修改）。

## 數據初始化

首次啟動時，後端以 `mysql` profile 運行，Flyway 會在 `MYSQL_DB`（默認 `nova_boot`）庫中自動執行版本化遷移：建表 + 種子數據（含管理員賬號與選單），無需手工執行任何 SQL。數據落在具名卷 `mysql-data`，重啟容器不丟失。

## 常用運維命令

```bash
docker compose logs -f backend     # 跟蹤後端日誌
docker compose ps                  # 查看各服務健康狀態
docker compose down                # 停止（保留數據卷）
docker compose down -v             # 停止並清空數據卷（危險：數據庫將重置）
docker compose up -d --build       # 改代碼後重新構建啟動
```

## 配置說明

- 後端數據源 / Redis 通過環境變量注入（`MYSQL_HOST` / `MYSQL_DB` / `MYSQL_PASSWORD` / `REDIS_HOST` 等，見 `application-mysql.example.yml` 的佔位符），全部由 `docker-compose.yml` 從 `.env` 插值，不寫死任何密碼。
- 後端構建（根目錄 `Dockerfile`）需要顯式傳遞 `-Drevision=1.0.0`：父 POM 版本為 `${revision}` 佔位符，子模組以 `1.0.0` 引用。
- 監控端點：`/actuator/health` 公開（無需登入，用於探活）；`/actuator/prometheus` 受登入攔截保護，抓取需攜帶登入 Token（詳見文檔站「Docker 部署」）。
- 建議（可選）在根目錄與 `web/` 添加 `.dockerignore`（忽略 `node_modules/`、`.m2repo/`、`.git/`、`target/`、`.pnpm-store/`、`nova-server-admin/src/main/resources/application-mysql.yml` 等），可顯著縮小構建上下文，並避免本機的 `application-mysql.yml`（含內網連接信息）被烤入鏡像。當前 `docker-compose.yml` 已通過環境變量覆蓋其運行時取值，但密碼仍會進入鏡像層，上線前建議補齊。
