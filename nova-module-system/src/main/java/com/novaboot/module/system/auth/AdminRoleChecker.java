package com.novaboot.module.system.auth;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.novaboot.module.system.entity.SysRole;
import com.novaboot.module.system.mapper.SysRoleMapper;
import com.novaboot.module.system.mapper.SysUserRoleMapper;
import com.novaboot.module.system.entity.SysUserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 內置超級管理員判定：角色編碼為 {@value #ADMIN_ROLE_CODE} 的用戶擁有全部權限，不受資料權限過濾
 */
@Component
@RequiredArgsConstructor
public class AdminRoleChecker {

    public static final String ADMIN_ROLE_CODE = "admin";

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    public boolean isAdminUser(long userId) {
        List<Long> roleIds = sysUserRoleMapper.selectList(
                        Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();
        return hasAdminRole(roleIds);
    }

    public boolean hasAdminRole(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }
        return sysRoleMapper.selectList(Wrappers.<SysRole>lambdaQuery().in(SysRole::getId, roleIds))
                .stream().anyMatch(r -> ADMIN_ROLE_CODE.equals(r.getCode()));
    }
}
