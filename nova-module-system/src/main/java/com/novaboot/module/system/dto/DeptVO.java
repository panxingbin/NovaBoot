package com.novaboot.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部門響應（樹形時帶 children）
 */
@Data
public class DeptVO {

    private Long id;

    private Long parentId;

    private String name;

    private Integer sortNo;

    private String leader;

    private String phone;

    private Integer status;

    private LocalDateTime createdAt;

    private List<DeptVO> children;
}
