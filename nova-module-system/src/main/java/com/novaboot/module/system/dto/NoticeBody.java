package com.novaboot.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通知公告創建/修改請求
 */
@Data
public class NoticeBody {

    @NotBlank(message = "標題不能為空")
    private String title;

    /** 類型：1通知 2公告 */
    @NotNull(message = "類型不能為空")
    private Integer type;

    private String content;

    @NotNull(message = "狀態不能為空")
    private Integer status;

    private String remark;
}
