package com.novaboot.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 開啟異步執行（日誌落庫等）
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
