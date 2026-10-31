package com.novaboot.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色創建/修改請求
 */
@Data
public class RoleBody {

    @NotBlank(message = "角色名稱不能為空")
    private String name;

    @NotBlank(message = "角色編碼不能為空")
    private String code;

    private Integer sortNo;

    @NotNull(message = "狀態不能為空")
    private Integer status;

    private String remark;
}
