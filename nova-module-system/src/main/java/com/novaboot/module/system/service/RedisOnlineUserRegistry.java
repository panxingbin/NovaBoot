package com.novaboot.module.system.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 在線用戶註冊表的 Redis 實現（nova.redis.enabled=true 時生效，跨實例共享）。
 * Hash 中的過期 token 由查詢側校驗 Sa-Token 會話後剔除。
 */
@Component
@ConditionalOnProperty(name = "nova.redis.enabled", havingValue = "true")
public class RedisOnlineUserRegistry implements OnlineUserRegistry {

    private static final String KEY = "nova:online";
    /** 與 sa-token 會話超時（30 天）對齊的兜底 TTL，過期 token 由查詢側剔除 */
    private static final Duration ENTRY_TTL = Duration.ofDays(30);

    private final StringRedisTemplate redisTemplate;

    public RedisOnlineUserRegistry(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void register(String token, long loginId) {
        OnlineEntry entry = new OnlineEntry(loginId, System.currentTimeMillis());
        // OnlineEntry 僅兩個 long 字段，用「loginId,loginTime」文本編碼，避免引入 JSON 依賴
        redisTemplate.opsForHash().put(KEY, token, entry.loginId() + "," + entry.loginTime());
        redisTemplate.expire(KEY, ENTRY_TTL);
    }

    @Override
    public void remove(String token) {
        redisTemplate.opsForHash().delete(KEY, token);
    }

    @Override
    public Map<String, OnlineEntry> snapshot() {
        Map<Object, Object> all = redisTemplate.opsForHash().entries(KEY);
        Map<String, OnlineEntry> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : all.entrySet()) {
            String[] parts = entry.getValue().toString().split(",");
            if (parts.length == 2) {
                try {
                    result.put(entry.getKey().toString(),
                            new OnlineEntry(Long.parseLong(parts[0]), Long.parseLong(parts[1])));
                } catch (NumberFormatException ignored) {
                    // 髒數據直接丟棄
                }
            }
        }
        return result;
    }
}
