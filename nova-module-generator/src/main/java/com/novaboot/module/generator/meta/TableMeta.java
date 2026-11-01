package com.novaboot.module.generator.meta;

import lombok.Data;

import java.util.List;

/**
 * 表元數據
 */
@Data
public class TableMeta {

    /** 表名 */
    private String tableName;

    /** 表註釋 */
    private String tableComment;

    private List<ColumnMeta> columns;

    /** 首個字符串列（生成列表頁搜索框用），可能為 null */
    private ColumnMeta searchColumn;
}
