package com.novaboot.module.system.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在線用戶註冊表：登入時登記 token → 用戶，單實例內存實現。
 * 過期/被強退的 token 由查詢側通過 getLoginIdByToken 校驗自動剔除。
 * M2 接入 Redis 後替換為共享註冊表即可支持水平擴展。
 */
@Component
public class OnlineUserRegistry {

    public record OnlineEntry(long loginId, long loginTime) {
    }

    private final Map<String, OnlineEntry> entries = new ConcurrentHashMap<>();

    public void register(String token, long loginId) {
        entries.put(token, new OnlineEntry(loginId, System.currentTimeMillis()));
    }

    public void remove(String token) {
        entries.remove(token);
    }

    public Map<String, OnlineEntry> snapshot() {
        return Map.copyOf(entries);
    }
}
