package com.novaboot.starter.ai;

import com.novaboot.common.exception.BusinessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * LLM 對話服務：調用 OpenAI 兼容的 /chat/completions 接口。
 * 未啟用（nova.ai.enabled=false）或未配置密鑰時拋出業務異常，不影響應用啟動。
 */
public class AiChatService {

    private final NovaAiProperties properties;

    private volatile RestClient restClient;

    public AiChatService(NovaAiProperties properties) {
        this.properties = properties;
    }

    /**
     * 單輪對話
     */
    public String chat(String prompt) {
        return chat(null, prompt);
    }

    /**
     * 帶系統提示詞的對話
     */
    public String chat(String systemPrompt, String prompt) {
        requireEnabled();
        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "temperature", properties.getTemperature(),
                "messages", messages(systemPrompt, prompt));
        Map<?, ?> response = client().post()
                .uri("/chat/completions")
                .body(body)
                .retrieve()
                .body(Map.class);
        return extractContent(response);
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<?, ?> response) {
        if (response == null) {
            throw new BusinessException("AI 網關返回空響應");
        }
        Object choices = response.get("choices");
        if (!(choices instanceof List<?> list) || list.isEmpty()) {
            throw new BusinessException("AI 網關未返回結果");
        }
        Object message = ((Map<String, Object>) list.get(0)).get("message");
        Object content = message instanceof Map<?, ?> m ? m.get("content") : null;
        if (content == null) {
            throw new BusinessException("AI 網關返回內容為空");
        }
        return content.toString();
    }

    private List<Map<String, String>> messages(String systemPrompt, String prompt) {
        if (StringUtils.hasText(systemPrompt)) {
            return List.of(Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", prompt));
        }
        return List.of(Map.of("role", "user", "content", prompt));
    }

    private RestClient client() {
        if (restClient == null) {
            synchronized (this) {
                if (restClient == null) {
                    restClient = RestClient.builder()
                            .baseUrl(properties.getBaseUrl())
                            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .build();
                }
            }
        }
        return restClient;
    }

    private void requireEnabled() {
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getApiKey())) {
            throw new BusinessException("AI 功能未啟用：請配置 nova.ai.enabled=true 與 nova.ai.api-key");
        }
    }
}
