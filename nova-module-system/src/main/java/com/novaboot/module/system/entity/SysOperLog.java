package com.novaboot.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日誌
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** 業務類型：0其它 1新增 2修改 3刪除 4授權 5強退 6上傳 */
    private Integer businessType;

    private String method;

    private String requestMethod;

    private String url;

    private String ip;

    private String param;

    private String errorMsg;

    /** 狀態：0成功 1失敗 */
    private Integer status;

    private Integer costMs;

    private String operName;

    private LocalDateTime createdAt;
}
