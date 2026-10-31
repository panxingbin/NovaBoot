package com.novaboot.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系統參數響應
 */
@Data
public class ConfigVO {

    private Long id;

    private String name;

    private String key;

    private String value;

    private Integer builtin;

    private String remark;

    private LocalDateTime createdAt;
}
