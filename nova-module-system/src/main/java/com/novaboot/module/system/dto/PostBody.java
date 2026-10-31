package com.novaboot.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 崗位創建/修改請求
 */
@Data
public class PostBody {

    @NotBlank(message = "崗位編碼不能為空")
    private String code;

    @NotBlank(message = "崗位名稱不能為空")
    private String name;

    private Integer sortNo;

    @NotNull(message = "狀態不能為空")
    private Integer status;

    private String remark;
}
