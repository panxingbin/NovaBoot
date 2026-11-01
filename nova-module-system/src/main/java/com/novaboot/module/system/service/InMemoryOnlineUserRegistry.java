package com.novaboot.module.system.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在線用戶註冊表：默認內存實現（單實例）
 */
@Component
@ConditionalOnProperty(name = "nova.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryOnlineUserRegistry implements OnlineUserRegistry {

    private final Map<String, OnlineEntry> entries = new ConcurrentHashMap<>();

    @Override
    public void register(String token, long loginId) {
        entries.put(token, new OnlineEntry(loginId, System.currentTimeMillis()));
    }

    @Override
    public void remove(String token) {
        entries.remove(token);
    }

    @Override
    public Map<String, OnlineEntry> snapshot() {
        return Map.copyOf(entries);
    }
}
