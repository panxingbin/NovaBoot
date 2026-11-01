package com.novaboot.framework.captcha;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.novaboot.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 圖形驗證碼（存儲按 nova.redis.enabled 自動選擇內存或 Redis）
 */
@Service
public class CaptchaService {

    private static final long EXPIRE_MILLIS = 5 * 60 * 1000;

    private final CaptchaStore captchaStore;

    @Value("${nova.captcha.enabled:true}")
    private boolean enabled;

    public CaptchaService(CaptchaStore captchaStore) {
        this.captchaStore = captchaStore;
    }

    public CaptchaVO generate() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(160, 48, 4, 20);
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        captchaStore.put(captchaId, captcha.getCode().toLowerCase(), EXPIRE_MILLIS);
        return new CaptchaVO(captchaId, captcha.getImageBase64Data());
    }

    public void verify(String captchaId, String captchaCode) {
        if (!enabled) {
            return;
        }
        if (captchaId == null || captchaId.isBlank() || captchaCode == null || captchaCode.isBlank()) {
            throw new BusinessException("驗證碼不能為空");
        }
        String code = captchaStore.take(captchaId);
        if (code == null) {
            throw new BusinessException("驗證碼已過期，請刷新後重試");
        }
        if (!code.equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException("驗證碼錯誤");
        }
    }
}
