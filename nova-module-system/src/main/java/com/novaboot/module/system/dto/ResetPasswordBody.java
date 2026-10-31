package com.novaboot.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重置密碼請求
 */
@Data
public class ResetPasswordBody {

    @NotBlank(message = "密碼不能為空")
    private String password;
}
