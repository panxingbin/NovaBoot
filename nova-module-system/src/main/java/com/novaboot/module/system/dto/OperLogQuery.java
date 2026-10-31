package com.novaboot.module.system.dto;

import com.novaboot.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日誌分頁查詢
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperLogQuery extends PageQuery {

    private String title;

    private String username;

    private Integer status;
}
