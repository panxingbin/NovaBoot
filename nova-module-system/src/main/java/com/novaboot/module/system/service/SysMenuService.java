package com.novaboot.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.dto.MenuBody;
import com.novaboot.module.system.dto.MenuVO;
import com.novaboot.module.system.entity.SysMenu;
import com.novaboot.module.system.entity.SysRoleMenu;
import com.novaboot.module.system.mapper.SysMenuMapper;
import com.novaboot.module.system.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 選單服務
 */
@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 路由用選單樹（僅啟用的目錄與選單，不含按鈕）
     */
    public List<MenuVO> tree() {
        List<SysMenu> menus = sysMenuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .eq(SysMenu::getStatus, 1)
                .ne(SysMenu::getMenuType, "B")
                .orderByAsc(SysMenu::getSortNo));
        return buildTree(menus);
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
