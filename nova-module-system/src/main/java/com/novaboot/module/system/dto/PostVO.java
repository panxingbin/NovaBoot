package com.novaboot.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 崗位響應
 */
@Data
public class PostVO {

    private Long id;

    private String code;

    private String name;

    private Integer sortNo;

    private Integer status;

    private String remark;

    private LocalDateTime createdAt;
}
