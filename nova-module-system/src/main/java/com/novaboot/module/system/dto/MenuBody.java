package com.novaboot.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 選單創建/修改請求
 */
@Data
public class MenuBody {

    @NotNull(message = "上級選單不能為空")
    private Long parentId;

    @NotBlank(message = "名稱不能為空")
    private String name;

    /** 類型：D目錄 M選單 B按鈕 */
    @NotBlank(message = "類型不能為空")
    private String menuType;

    private String path;

    private String component;

    private String perm;

    private String icon;

    private Integer sortNo;

    private Integer visible;

    @NotNull(message = "狀態不能為空")
    private Integer status;
}
