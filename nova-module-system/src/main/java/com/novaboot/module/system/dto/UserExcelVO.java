package com.novaboot.module.system.dto;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 用戶 Excel 行（導入/導出）
 */
@Data
public class UserExcelVO {

    @ExcelProperty("用戶名")
    private String username;

    @ExcelProperty("暱稱")
    private String nickname;

    @ExcelProperty("郵箱")
    private String email;

    @ExcelProperty("手機號")
    private String phone;

    @ExcelProperty("狀態(1啟用 0停用)")
    private Integer status;

    @ExcelProperty("備註")
    private String remark;
}
