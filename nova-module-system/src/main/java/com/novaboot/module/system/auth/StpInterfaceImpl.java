package com.novaboot.module.system.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.novaboot.module.system.entity.SysMenu;
import com.novaboot.module.system.entity.SysRole;
import com.novaboot.module.system.entity.SysRoleMenu;
import com.novaboot.module.system.entity.SysUserRole;
import com.novaboot.module.system.mapper.SysMenuMapper;
import com.novaboot.module.system.mapper.SysRoleMapper;
import com.novaboot.module.system.mapper.SysRoleMenuMapper;
import com.novaboot.module.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 權限數據源：角色與權限字元均來自 RBAC 表；超級管理員返回通配權限
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysMenuMapper sysMenuMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<Long> roleIds = roleIdsOf(Long.valueOf(loginId.toString()));
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        if (isAdmin(roleIds)) {
            return List.of("*:*:*");
        }
        List<Long> menuIds = sysRoleMenuMapper.selectList(
                        Wrappers.<SysRoleMenu>lambdaQuery().in(SysRoleMenu::getRoleId, roleIds))
                .stream().map(SysRoleMenu::getMenuId).distinct().toList();
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sysMenuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                        .in(SysMenu::getId, menuIds)
                        .isNotNull(SysMenu::getPerm))
                .stream()
                .map(SysMenu::getPerm)
                .filter(p -> p != null && !p.isBlank())
                .distinct()
                .toList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<Long> roleIds = roleIdsOf(Long.valueOf(loginId.toString()));
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sysRoleMapper.selectList(Wrappers.<SysRole>lambdaQuery().in(SysRole::getId, roleIds))
                .stream().map(SysRole::getCode).toList();
    }

    private boolean isAdmin(List<Long> roleIds) {
        return sysRoleMapper.selectList(Wrappers.<SysRole>lambdaQuery().in(SysRole::getId, roleIds))
                .stream().anyMatch(r -> "admin".equals(r.getCode()));
    }

    private List<Long> roleIdsOf(Long userId) {
        return sysUserRoleMapper.selectList(
                        Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();
    }
}
