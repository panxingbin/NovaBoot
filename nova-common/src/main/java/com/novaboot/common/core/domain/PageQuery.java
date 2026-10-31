package com.novaboot.common.core.domain;

import lombok.Data;

/**
 * 分頁查詢參數
 */
@Data
public class PageQuery {

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
