package com.novaboot.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用戶響應
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private Long deptId;

    private String email;

    private String phone;

    private Integer status;

    private String remark;

    private List<Long> roleIds;

    private List<Long> postIds;

    private LocalDateTime createdAt;
}
