package com.novaboot.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色響應
 */
@Data
public class RoleVO {

    private Long id;

    private String name;

    private String code;

    private Integer sortNo;

    private Integer status;

    /** 數據範圍：1全部 2自訂部門 3本部門 4本部門及以下 5僅本人 */
    private Integer dataScope;

    private String remark;

    private LocalDateTime createdAt;
}
