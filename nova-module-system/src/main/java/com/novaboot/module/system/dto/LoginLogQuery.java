package com.novaboot.module.system.dto;

import com.novaboot.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登入日誌分頁查詢
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogQuery extends PageQuery {

    private String username;

    private Integer status;
}
