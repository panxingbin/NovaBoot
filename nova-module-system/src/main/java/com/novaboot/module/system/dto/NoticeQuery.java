package com.novaboot.module.system.dto;

import com.novaboot.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告分頁查詢
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeQuery extends PageQuery {

    private String keyword;
}
