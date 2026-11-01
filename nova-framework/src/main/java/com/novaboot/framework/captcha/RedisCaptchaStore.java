package com.novaboot.framework.captcha;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 驗證碼存儲（nova.redis.enabled=true 時生效，TTL 到期自動清理）
 */
@Component
@ConditionalOnProperty(name = "nova.redis.enabled", havingValue = "true")
public class RedisCaptchaStore implements CaptchaStore {

    private static final String KEY_PREFIX = "nova:captcha:";

    private final StringRedisTemplate redisTemplate;

    public RedisCaptchaStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void put(String captchaId, String code, long ttlMillis) {
        redisTemplate.opsForValue().set(KEY_PREFIX + captchaId, code, Duration.ofMillis(ttlMillis));
    }

    @Override
    public String take(String captchaId) {
        return redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + captchaId);
    }
}
