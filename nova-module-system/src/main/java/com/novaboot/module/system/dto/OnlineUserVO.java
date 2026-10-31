package com.novaboot.module.system.dto;

import lombok.Data;

/**
 * 在線用戶響應
 */
@Data
public class OnlineUserVO {

    private String token;

    private String username;

    private String nickname;

    private String device;

    /** 登入時間（ISO-8601 字符串） */
    private String loginTime;
}
