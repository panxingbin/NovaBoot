package com.novaboot.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 部門創建/修改請求
 */
@Data
public class DeptBody {

    @NotNull(message = "上級部門不能為空")
    private Long parentId;

    @NotBlank(message = "部門名稱不能為空")
    private String name;

    private Integer sortNo;

    private String leader;

    private String phone;

    @NotNull(message = "狀態不能為空")
    private Integer status;
}
