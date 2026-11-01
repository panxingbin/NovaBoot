package com.novaboot.framework.captcha;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 內存驗證碼存儲（默認，單實例）
 */
@Component
@ConditionalOnProperty(name = "nova.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryCaptchaStore implements CaptchaStore {

    private record Entry(String code, long expireAt) {
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public void put(String captchaId, String code, long ttlMillis) {
        store.put(captchaId, new Entry(code, System.currentTimeMillis() + ttlMillis));
        store.values().removeIf(e -> e.expireAt() < System.currentTimeMillis());
    }

    @Override
    public String take(String captchaId) {
        Entry entry = store.remove(captchaId);
        if (entry == null || entry.expireAt() < System.currentTimeMillis()) {
            return null;
        }
        return entry.code();
    }
}
