package com.novaboot.framework.captcha;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.novaboot.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 圖形驗證碼（M0 單實例使用內存存儲，M1 接入 Redis 後可水平擴展）
 */
@Service
public class CaptchaService {

    private static final long EXPIRE_MILLIS = 5 * 60 * 1000;

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    @Value("${nova.captcha.enabled:true}")
    private boolean enabled;

    public CaptchaVO generate() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(160, 48, 4, 20);
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        store.put(captchaId, new Entry(captcha.getCode().toLowerCase(), System.currentTimeMillis() + EXPIRE_MILLIS));
        store.values().removeIf(e -> e.expireAt() < System.currentTimeMillis());
        return new CaptchaVO(captchaId, captcha.getImageBase64Data());
    }

    public void verify(String captchaId, String captchaCode) {
        if (!enabled) {
            return;
        }
        if (captchaId == null || captchaId.isBlank() || captchaCode == null || captchaCode.isBlank()) {
            throw new BusinessException("驗證碼不能為空");
        }
        Entry entry = store.remove(captchaId);
        if (entry == null || entry.expireAt() < System.currentTimeMillis()) {
            throw new BusinessException("驗證碼已過期，請刷新後重試");
        }
        if (!entry.code().equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException("驗證碼錯誤");
        }
    }

    private record Entry(String code, long expireAt) {
    }
}
