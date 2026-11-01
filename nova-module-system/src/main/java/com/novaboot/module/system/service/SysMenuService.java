package com.novaboot.module.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.auth.AdminRoleChecker;
import com.novaboot.module.system.dto.MenuBody;
import com.novaboot.module.system.dto.MenuVO;
import com.novaboot.module.system.entity.SysMenu;
import com.novaboot.module.system.entity.SysRoleMenu;
import com.novaboot.module.system.entity.SysUserRole;
import com.novaboot.module.system.enums.MenuType;
import com.novaboot.module.system.mapper.SysMenuMapper;
import com.novaboot.module.system.mapper.SysRoleMenuMapper;
import com.novaboot.module.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 選單服務
 */
@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final AdminRoleChecker adminRoleChecker;

    /**
     * 路由用選單樹（僅啟用的目錄與選單，不含按鈕）。
     * 按當前登入用戶的角色過濾：超級管理員可見全部，普通用戶僅可見被授權的選單；
     * 若只授權了子選單而未授權父目錄，自動補全祖先鏈避免菜單懸空。
     */
    public List<MenuVO> tree() {
        List<SysMenu> menus = sysMenuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .eq(SysMenu::getStatus, 1)
                .ne(SysMenu::getMenuType, MenuType.BUTTON.getCode())
                .orderByAsc(SysMenu::getSortNo));
        List<SysMenu> visible = isAdmin() ? menus : filterByRoles(menus);
        return buildTree(visible);
    }

    private boolean isAdmin() {
        return adminRoleChecker.isAdminUser(StpUtil.getLoginIdAsLong());
    }

    /**
     * 按角色選單授權過濾，並補全被授權節點的祖先目錄
     */
    private List<SysMenu> filterByRoles(List<SysMenu> all) {
        List<Long> roleIds = sysUserRoleMapper.selectList(
                        Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, StpUtil.getLoginIdAsLong()))
                .stream().map(SysUserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        Set<Long> boundMenuIds = sysRoleMenuMapper.selectList(
                        Wrappers.<SysRoleMenu>lambdaQuery().in(SysRoleMenu::getRoleId, roleIds))
                .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
        if (boundMenuIds.isEmpty()) {
            return List.of();
        }
        Map<Long, SysMenu> byId = all.stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m));
        Set<Long> visibleIds = new HashSet<>();
        for (Long menuId : boundMenuIds) {
            SysMenu cur = byId.get(menuId);
            while (cur != null && visibleIds.add(cur.getId())) {
                cur = byId.get(cur.getParentId());
            }
        }
        return all.stream().filter(m -> visibleIds.contains(m.getId())).toList();
    }

    /**
     * 管理用平鋪列表（含按鈕）
     */
    public List<MenuVO> list(String keyword) {
        List<SysMenu> menus = sysMenuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .like(StringUtils.hasText(keyword), SysMenu::getName, keyword)
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getSortNo));
        return menus.stream().map(m -> toVO(m, null)).toList();
    }

    @Transactional
    public void create(MenuBody body) {
        SysMenu menu = new SysMenu();
        copyBody(menu, body);
        sysMenuMapper.insert(menu);
    }

    @Transactional
    public void update(Long id, MenuBody body) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("選單不存在");
        }
        if (id.equals(body.getParentId())) {
            throw new BusinessException("上級選單不能是自身");
        }
        copyBody(menu, body);
        sysMenuMapper.updateById(menu);
    }

    @Transactional
    public void delete(Long id) {
        Long children = sysMenuMapper.selectCount(
                Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, id));
        if (children != null && children > 0) {
            throw new BusinessException("存在子選單，不允許刪除");
        }
        sysMenuMapper.deleteById(id);
        sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getMenuId, id));
    }

    private List<MenuVO> buildTree(List<SysMenu> menus) {
        Map<Long, List<SysMenu>> byParent = menus.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        return assemble(byParent, 0L);
    }

    private List<MenuVO> assemble(Map<Long, List<SysMenu>> byParent, Long parentId) {
        List<MenuVO> result = new ArrayList<>();
        for (SysMenu menu : byParent.getOrDefault(parentId, List.of())) {
            result.add(toVO(menu, assemble(byParent, menu.getId())));
        }
        return result;
    }

    private void copyBody(SysMenu menu, MenuBody body) {
        menu.setParentId(body.getParentId());
        menu.setName(body.getName());
        menu.setMenuType(body.getMenuType());
        menu.setPath(body.getPath());
        menu.setComponent(body.getComponent());
        menu.setPerm(body.getPerm());
        menu.setIcon(body.getIcon());
        menu.setSortNo(body.getSortNo() == null ? 0 : body.getSortNo());
        menu.setVisible(body.getVisible() == null ? 1 : body.getVisible());
        menu.setStatus(body.getStatus());
    }

    private MenuVO toVO(SysMenu menu, List<MenuVO> children) {
        MenuVO vo = new MenuVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setName(menu.getName());
        vo.setMenuType(menu.getMenuType());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setPerm(menu.getPerm());
        vo.setIcon(menu.getIcon());
        vo.setSortNo(menu.getSortNo());
        vo.setVisible(menu.getVisible());
        vo.setStatus(menu.getStatus());
        vo.setCreatedAt(menu.getCreatedAt());
        vo.setChildren(children);
        return vo;
    }
}
