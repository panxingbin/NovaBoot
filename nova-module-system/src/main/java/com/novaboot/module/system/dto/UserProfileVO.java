package com.novaboot.module.system.dto;

import java.util.List;

/**
 * 當前登入用戶信息
 */
public record UserProfileVO(UserInfo user, List<String> roles, List<String> permissions) {

    public record UserInfo(Long id, String username, String nickname) {
    }
}
