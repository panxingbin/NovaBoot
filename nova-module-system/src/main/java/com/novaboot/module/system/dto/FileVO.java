package com.novaboot.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 檔案響應
 */
@Data
public class FileVO {

    private Long id;

    private String name;

    private String originalName;

    private Long size;

    private String contentType;

    private String operName;

    private LocalDateTime createdAt;
}
