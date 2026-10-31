package com.novaboot.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 用戶創建/修改請求
 */
@Data
public class UserBody {

    /** 僅創建時必填 */
    private String username;

    /** 僅創建時必填 */
    private String password;

    @NotBlank(message = "暱稱不能為空")
    private String nickname;

    private Long deptId;

    private String email;

    private String phone;

    @NotNull(message = "狀態不能為空")
    private Integer status;

    private String remark;

    private List<Long> roleIds;

    private List<Long> postIds;
}
