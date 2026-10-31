package com.novaboot.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局錯誤碼
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "success"),
    BAD_REQUEST(400, "請求參數錯誤"),
    UNAUTHORIZED(401, "未登入或登入已過期"),
    FORBIDDEN(403, "沒有操作權限"),
    ERROR(500, "系統內部錯誤");

    private final int code;
    private final String message;
}
