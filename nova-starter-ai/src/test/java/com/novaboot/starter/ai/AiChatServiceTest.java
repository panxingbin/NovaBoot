package com.novaboot.starter.ai;

import com.novaboot.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AI Starter 未啟用狀態行為測試（不發起網絡請求）
 */
class AiChatServiceTest {

    @Test
    void disabledByDefaultAndThrowsWithGuidance() {
        NovaAiProperties properties = new NovaAiProperties();
        AiChatService service = new AiChatService(properties);

        BusinessException e = assertThrows(BusinessException.class, () -> service.chat("你好"));
        assertTrue(e.getMessage().contains("nova.ai.enabled"));
    }

    @Test
    void enabledWithoutApiKeyStillThrows() {
        NovaAiProperties properties = new NovaAiProperties();
        properties.setEnabled(true);
        properties.setApiKey("  ");
        AiChatService service = new AiChatService(properties);

        BusinessException e = assertThrows(BusinessException.class, () -> service.chat("你好"));
        assertTrue(e.getMessage().contains("api-key"));
    }

    @Test
    void defaultsAreSane() {
        NovaAiProperties properties = new NovaAiProperties();
        assertEquals("https://api.openai.com/v1", properties.getBaseUrl());
        assertEquals("gpt-4o-mini", properties.getModel());
        assertEquals(60, properties.getTimeoutSeconds());
        assertEquals(60, properties.timeout().toSeconds());
    }
}
