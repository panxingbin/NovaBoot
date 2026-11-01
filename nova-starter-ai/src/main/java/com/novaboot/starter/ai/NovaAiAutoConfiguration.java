package com.novaboot.starter.ai;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * AI Starter 自動配置：僅註冊服務，未啟用時調用方收到明確的業務異常
 */
@AutoConfiguration
@EnableConfigurationProperties(NovaAiProperties.class)
public class NovaAiAutoConfiguration {

    @Bean
    public AiChatService aiChatService(NovaAiProperties properties) {
        return new AiChatService(properties);
    }
}
