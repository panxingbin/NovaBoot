package com.novaboot.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 系統參數創建/修改請求
 */
@Data
public class ConfigBody {

    @NotBlank(message = "參數名稱不能為空")
    private String name;

    @NotBlank(message = "參數鍵名不能為空")
    private String key;

    @NotBlank(message = "參數鍵值不能為空")
    private String value;

    private Integer builtin;

    private String remark;
}
