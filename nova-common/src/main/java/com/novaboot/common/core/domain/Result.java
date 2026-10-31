package com.novaboot.common.core.domain;

import com.novaboot.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 統一響應體
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        return build(ErrorCode.SUCCESS.getCode(), "success", null);
    }

    public static <T> Result<T> ok(T data) {
        return build(ErrorCode.SUCCESS.getCode(), "success", data);
    }

    public static <T> Result<T> fail(String message) {
        return build(ErrorCode.ERROR.getCode(), message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return build(code, message, null);
    }

    public static <T> Result<T> build(int code, String message, T data) {
        return new Result<>(code, message, data);
    }
}
