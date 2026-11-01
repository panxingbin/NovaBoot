package com.novaboot.module.system.service;

/**
 * 在線用戶註冊表：登入時登記 token → 用戶。
 * 默認內存實現（單實例）；nova.redis.enabled=true 時由 Redis 實現承接，跨實例共享。
 * 過期/被強退的 token 由查詢側通過 getLoginIdByToken 校驗自動剔除。
 */
public interface OnlineUserRegistry {

    void register(String token, long loginId);

    void remove(String token);

    /**
     * token → 登入信息的只讀快照
     */
    java.util.Map<String, OnlineEntry> snapshot();

    /**
     * 單條在線記錄
     */
    record OnlineEntry(long loginId, long loginTime) {
    }
}
