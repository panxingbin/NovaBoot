package com.novaboot.module.generator.meta;

import lombok.Data;

/**
 * 表字段元數據（information_schema 採集 + 生成用推導欄位）
 */
@Data
public class ColumnMeta {

    /** 數據庫列名（下劃線） */
    private String columnName;

    /** Java 屬性名（小駝峰） */
    private String javaName;

    /** Java 類型（String/Long/Integer/LocalDateTime/...） */
    private String javaType;

    /** 數據庫原始類型描述（如 varchar(50)） */
    private String columnType;

    /** 列註釋 */
    private String comment;

    /** 是否主鍵 */
    private boolean pk;

    /** 是否允許 NULL */
    private boolean nullable;

    /** 是否出現在新增/編輯表單（主鍵與審計欄位排除） */
    private boolean formField;

    /** 是否作為列表搜索欄位候選（首個字符串列） */
    private boolean searchCandidate;
}
