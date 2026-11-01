package com.novaboot.module.generator.dto;

import com.novaboot.common.exception.BusinessException;
import lombok.Data;

import java.util.Locale;

/**
 * 代碼生成選項
 */
@Data
public class GenOptions {

    /** 表名 */
    private String tableName;

    /** 業務模塊名（包段/路由段，小寫字母開頭，如 biz） */
    private String moduleName;

    /** 業務名（類名前綴，小駝峰，如 config → Config/ConfigService...） */
    private String businessName;

    /** 功能名（中文，用於註釋與選單名） */
    private String functionName;

    /** 作者 */
    private String author = "novaboot";

    /** 生成選單的起始 ID（目錄=起始，頁面=+1，按鈕=+2..+4） */
    private Long menuStartId = 1000L;

    public void setTableName(String tableName) {
        if (tableName == null || !tableName.matches("[a-zA-Z][a-zA-Z0-9_]{0,63}")) {
            throw new BusinessException("表名不合法");
        }
        this.tableName = tableName;
    }

    public void setModuleName(String moduleName) {
        if (moduleName == null || !moduleName.matches("[a-z][a-z0-9]{0,31}")) {
            throw new BusinessException("模塊名不合法（小寫字母開頭，僅含小寫字母/數字）");
        }
        this.moduleName = moduleName;
    }

    public void setBusinessName(String businessName) {
        if (businessName == null || !businessName.matches("[a-z][a-zA-Z0-9]{0,63}")) {
            throw new BusinessException("業務名不合法（小寫字母開頭的小駝峰）");
        }
        this.businessName = businessName;
    }

    public String getPackageName() {
        return "com.novaboot.module." + moduleName;
    }

    public String getClassName() {
        return Character.toUpperCase(businessName.charAt(0)) + businessName.substring(1);
    }

    public String getRoutePath() {
        return "/" + moduleName + "/" + businessName;
    }

    public String getApiPath() {
        return "/api/" + moduleName + "/" + businessName.toLowerCase(Locale.ROOT) + "s";
    }
}
