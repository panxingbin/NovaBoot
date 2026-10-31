package com.novaboot.framework.captcha;

/**
 * 驗證碼響應（imageBase64 為完整 data URI，可直接綁定到 img 標籤）
 */
public record CaptchaVO(String captchaId, String imageBase64) {
}
