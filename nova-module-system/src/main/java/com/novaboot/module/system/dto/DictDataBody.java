package com.novaboot.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 字典數據創建/修改請求
 */
@Data
public class DictDataBody {

    @NotBlank(message = "字典類型不能為空")
    private String dictType;

    @NotBlank(message = "標籤不能為空")
    private String label;

    @NotBlank(message = "鍵值不能為空")
    private String value;

    private Integer sortNo;

    @NotNull(message = "狀態不能為空")
    private Integer status;

    private String remark;
}
