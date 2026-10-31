package com.novaboot.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知公告響應
 */
@Data
public class NoticeVO {

    private Long id;

    private String title;

    /** 類型：1通知 2公告 */
    private Integer type;

    private String content;

    private Integer status;

    private String remark;

    private LocalDateTime createdAt;
}
