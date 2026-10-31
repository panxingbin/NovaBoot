package com.novaboot.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典數據響應
 */
@Data
public class DictDataVO {

    private Long id;

    private String dictType;

    private String label;

    private String value;

    private Integer sortNo;

    private Integer status;

    private String remark;

    private LocalDateTime createdAt;
}
