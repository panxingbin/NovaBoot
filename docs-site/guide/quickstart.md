# 快速開始

NovaBoot 後端默認以 `local` profile 啟動：**H2 內存庫 + 內存會話**，零外部依賴，克隆即可跑。以下假設你已安裝 JDK 21+、Maven 3.9+、Node.js 24 LTS 與 pnpm 11。

## 後端（零依賴啟動）

```bash
cd nova-server-admin
mvn -Drevision=1.0.0 package -DskipTests
mvn spring-boot:run                  # 端口 8080
```

::: tip 為什麼要帶 -Drevision
父 POM（根目錄 `pom.xml`）的版本為 CI-friendly 佔位符 `${revision}`，各子模組以 `1.0.0` 引用父 POM，構建時需顯式傳入 `-Drevision=1.0.0` 對齊版本。
:::

`local` profile 的行為（見 `application-local.yml`）：

- 數據源為 H2 內存庫（MySQL 兼容模式），Flyway 自動建表與寫入種子數據，重啟即重置；
- 會話 / 驗證碼 / 在線用戶走框架默認的**內存實現**，不依賴 Redis；
- 行為驗證碼默認關閉（`nova.captcha.enabled=false`），便於本地聯調與冒煙測試。

啟動成功後：

- 介面文檔（Swagger UI）：<http://localhost:8080/swagger-ui.html>
- 健康檢查：<http://localhost:8080/actuator/health>

## 後端（MySQL + Redis）

生產或長期開發建議切到 `mysql` profile：會話 / 驗證碼 / 在線用戶存 Redis（`nova.redis.enabled=true`），Flyway 在 MySQL 中執行版本化遷移。

兩種方式任選：

1. **配置文件**：複製 `application-mysql.example.yml` 為同目錄 `application-mysql.yml`，填入你的連接信息（該文件已 gitignore，不會入庫）；
2. **環境變量**：不改文件，直接注入 `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DB` / `MYSQL_USERNAME` / `MYSQL_PASSWORD` / `REDIS_HOST` / `REDIS_PORT`。

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql \
    -Dspring-boot.run.jvmArguments="-DMYSQL_HOST=localhost -DMYSQL_PASSWORD=你的密碼 -DREDIS_HOST=localhost"
```

建表與種子數據由 Flyway 自動執行，無需手工跑 SQL。Docker Compose 部署即是通過環境變量注入的，見 [Docker 部署](./deploy)。

## 前端

```bash
cd web
pnpm install
pnpm dev     # 開發模式，端口 5173，/api 代理到 localhost:8080
pnpm build   # 產物在 web/apps/admin/dist
```

::: tip
前端未鎖定 `packageManager`，本倉庫按 pnpm 11 開發；`pnpm build` 即 `vue-tsc -b && vite build`，含 TypeScript 全量類型檢查。
:::

## 登入

開發服務器 <http://localhost:5173>（或直連後端部署時的站點），默認賬號：

| 項 | 值 |
|---|---|
| 賬號 | `admin` |
| 初始密碼 | `admin123`（對應 `application.yml` 的 `nova.init.admin-password`，登入後請立即修改） |

## 下一步

- [主題切換](./themes)：四套主題與 `?theme=` 深鏈接
- [多語言](./i18n)：三語言與 `?lang=` 深鏈接
- [代碼生成](./generator)：從業務表一鍵生成前後端 CRUD
- [Docker 部署](./deploy)：一鍵拉起 MySQL + Redis + 後端 + 前端
