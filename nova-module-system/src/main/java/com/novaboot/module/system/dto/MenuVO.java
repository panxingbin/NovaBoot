package com.novaboot.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 選單響應（樹形時帶 children）
 */
@Data
public class MenuVO {

    private Long id;

    private Long parentId;

    private String name;

    /** 類型：D目錄 M選單 B按鈕 */
    private String menuType;

    private String path;

    private String component;

    private String perm;

    private String icon;

    private Integer sortNo;

    private Integer visible;

    private Integer status;

    private LocalDateTime createdAt;

    private List<MenuVO> children;
}
