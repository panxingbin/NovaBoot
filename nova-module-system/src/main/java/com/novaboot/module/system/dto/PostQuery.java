package com.novaboot.module.system.dto;

import com.novaboot.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 崗位分頁查詢
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostQuery extends PageQuery {

    private String keyword;
}
