package com.novaboot.starter.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * AI 網關配置（OpenAI 兼容接口）
 *
 * <pre>
 * nova:
 *   ai:
 *     enabled: true
 *     base-url: https://api.openai.com/v1   # 或任意兼容網關（DeepSeek/Moonshot/自建網關）
 *     api-key: sk-xxx
 *     model: gpt-4o-mini
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "nova.ai")
public class NovaAiProperties {

    /** 是否啟用（默認關閉，未配置密鑰時應用可正常啟動） */
    private boolean enabled = false;

    private String baseUrl = "https://api.openai.com/v1";

    private String apiKey;

    private String model = "gpt-4o-mini";

    private double temperature = 0.7;

    /** 請求超時（秒） */
    private long timeoutSeconds = 60;

    public Duration timeout() {
        return Duration.ofSeconds(timeoutSeconds);
    }
}
