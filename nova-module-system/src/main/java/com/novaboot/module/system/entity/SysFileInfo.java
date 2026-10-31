package com.novaboot.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 檔案信息
 */
@Data
@TableName("sys_file_info")
public class SysFileInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String originalName;

    private String path;

    private Long size;

    private String contentType;

    private String operName;

    private LocalDateTime createdAt;
}
