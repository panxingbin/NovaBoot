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

    private String remark;

    private LocalDateTime createdAt;
}
