# AI 與郵件 Starter

NovaBoot 以 Spring Boot Starter 形式提供平台能力擴展，`nova-starter-ai` 與 `nova-starter-mail` 是其中的 AI 對話與郵件發送能力。兩者都是**可選能力**：未配置時應用正常啟動，僅在調用時返回明確的業務錯誤，不會拖垮主體功能。

## AI Starter（nova-starter-ai）

對接 **OpenAI 兼容網關**的對話能力，配置前綴 `nova.ai.*`（見 `application.yml`）：

| 配置 | 默認值 | 說明 |
|---|---|---|
| `nova.ai.enabled` | `false` | 總開關，默認關閉 |
| `nova.ai.base-url` | `https://api.openai.com/v1` | 網關地址，可用環境變量 `AI_BASE_URL` 覆蓋 |
| `nova.ai.api-key` | 空 | 密鑰，可用環境變量 `AI_API_KEY` 注入 |
| `nova.ai.model` | `gpt-4o-mini` | 模型名，可用環境變量 `AI_MODEL` 覆蓋 |
| `nova.ai.temperature` | `0.7` | 採樣溫度 |
| `nova.ai.timeout-seconds` | `60` | 請求超時 |

啟用示例：

```yaml
nova:
  ai:
    enabled: true
    base-url: https://your-gateway.example.com/v1
    api-key: ${AI_API_KEY}
    model: your-model
```

### 接口：/api/ai/chat

```bash
curl -X POST http://localhost:8080/api/ai/chat \
     -H "Authorization: Bearer <登入Token>" \
     -H "Content-Type: application/json" \
     -d '{"prompt": "用一句話介紹 NovaBoot"}'
```

- 需要 `ai:chat` 權限（種子數據已掛在「系統工具」下並授權超級管理員）；
- **優雅降級**：`nova.ai.enabled=false`（默認）時控制器不註冊，`/api/ai/chat` 返回 404；`enabled=true` 但未配置 `api-key` 時，應用正常啟動，調用返回業務錯誤「AI 功能未啟用：請配置 nova.ai.enabled=true 與 nova.ai.api-key」。

## 郵件 Starter（nova-starter-mail）

基於 Spring Mail 的郵件發送能力，SMTP 連接走標準的 `spring.mail.*` 配置：

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 465
    username: no-reply@example.com
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.ssl.enable: true

nova:
  mail:
    # 發件人；未配置時回退 spring.mail.username
    from: no-reply@example.com
```

### 接口：/api/mail/send

```bash
curl -X POST http://localhost:8080/api/mail/send \
     -H "Authorization: Bearer <登入Token>" \
     -H "Content-Type: application/json" \
     -d '{"to": "user@example.com", "subject": "測試", "content": "<b>Hello</b>", "html": true}'
```

- 請求體：`to`（必填）、`subject`、`content`、`html`（`true` 走 HTML 郵件，否則純文本）；
- 需要 `mail:send` 權限（種子數據已授權超級管理員）；
- **優雅降級**：未配置 `spring.mail.host` 時控制器不註冊（`/api/mail/send` 返回 404），應用正常啟動；若繞過控制器直接調用 `MailService`，會拋出業務錯誤「郵件未配置：請設置 spring.mail.host 等參數」；缺發件人時提示配置 `nova.mail.from` 或 `spring.mail.username`。

## 設計要點

- 兩個 Starter 通過 `AutoConfiguration.imports` 自動裝配，控制器用 `@ConditionalOnProperty` 按配置條件註冊——**未啟用不佔用任何端點**；
- 錯誤統一拋 `BusinessException`（返回明確的業務錯誤信息），不影響應用啟動與其他模塊；
- 演示控制器位於 `nova-server-admin`（`AiChatController` / `MailController`），業務系統可直接注入 `AiChatService` / `MailService` 復用能力。
