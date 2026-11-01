package com.novaboot.module.system.enums;

import lombok.Getter;

/**
 * 選單類型
 */
@Getter
public enum MenuType {

    /** 目錄 */
    DIR("D"),
    /** 選單 */
    MENU("M"),
    /** 按鈕（掛權限字元，不產生路由） */
    BUTTON("B");

    private final String code;

    MenuType(String code) {
        this.code = code;
    }
}
