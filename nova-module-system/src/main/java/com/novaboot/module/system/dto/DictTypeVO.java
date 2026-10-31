package com.novaboot.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典類型響應
 */
@Data
public class DictTypeVO {

    private Long id;

    private String name;

    private String type;

    private String remark;

    private LocalDateTime createdAt;
}
