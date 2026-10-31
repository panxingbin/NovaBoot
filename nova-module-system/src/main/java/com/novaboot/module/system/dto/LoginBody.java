package com.novaboot.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登入請求
 */
@Data
public class LoginBody {

    @NotBlank(message = "用戶名不能為空")
    private String username;

    @NotBlank(message = "密碼不能為空")
    private String password;

    private String captchaId;

    private String captchaCode;
}
