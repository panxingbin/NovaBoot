package com.novaboot.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典類型創建/修改請求
 */
@Data
public class DictTypeBody {

    @NotBlank(message = "字典名稱不能為空")
    private String name;

    @NotBlank(message = "字典類型不能為空")
    private String type;

    private String remark;
}
