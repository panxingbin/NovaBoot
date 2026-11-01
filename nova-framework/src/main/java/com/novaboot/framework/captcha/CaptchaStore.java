package com.novaboot.framework.captcha;

/**
 * 驗證碼存儲抽象：local profile 用內存實現，啟用 Redis 後由 Redis 實現承接（支持水平擴展）
 */
public interface CaptchaStore {

    void put(String captchaId, String code, long ttlMillis);

    /**
     * 取出並刪除（驗證碼一次性使用）；不存在返回 null
     */
    String take(String captchaId);
}
