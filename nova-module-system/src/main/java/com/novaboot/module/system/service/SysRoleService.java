package com.novaboot.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.dto.RoleBody;
import com.novaboot.module.system.dto.RoleQuery;
import com.novaboot.module.system.dto.RoleVO;
import com.novaboot.module.system.entity.SysRole;
import com.novaboot.module.system.entity.SysRoleMenu;
import com.novaboot.module.system.entity.SysUserRole;
import com.novaboot.module.system.mapper.SysRoleMapper;
import com.novaboot.module.system.mapper.SysRoleMenuMapper;
import com.novaboot.module.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色服務
 */
@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public PageResult<RoleVO> page(RoleQuery query) {
        Page<SysRole> page = sysRoleMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                Wrappers.<SysRole>lambdaQuery()
                        .and(StringUtils.hasText(query.getKeyword()), w -> w
                                .like(SysRole::getName, query.getKeyword())
                                .or().like(SysRole::getCode, query.getKeyword()))
                        .orderByAsc(SysRole::getSortNo));
        List<RoleVO> records = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), records);
    }

    @Transactional
    public void create(RoleBody body) {
        checkCodeUnique(body.getCode(), null);
        SysRole role = new SysRole();
        role.setName(body.getName());
        role.setCode(body.getCode());
        role.setSortNo(body.getSortNo() == null ? 0 : body.getSortNo());
        role.setStatus(body.getStatus());
        role.setRemark(body.getRemark());
        sysRoleMapper.insert(role);
    }

    @Transactional
    public void update(Long id, RoleBody body) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        checkCodeUnique(body.getCode(), id);
        if (id == 1L && !"admin".equals(body.getCode())) {
            throw new BusinessException("內置管理員角色編碼不允許修改");
        }
        role.setName(body.getName());
        role.setCode(body.getCode());
        role.setSortNo(body.getSortNo() == null ? 0 : body.getSortNo());
        role.setStatus(body.getStatus());
        role.setRemark(body.getRemark());
        sysRoleMapper.updateById(role);
    }

    @Transactional
    public void delete(Long id) {
        if (id == 1L) {
            throw new BusinessException("內置管理員角色不允許刪除");
        }
        sysRoleMapper.deleteById(id);
        sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, id));
        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, id));
    }

    public List<Long> getMenuIds(Long roleId) {
        return sysRoleMenuMapper.selectList(
                        Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).toList();
    }

    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            sysRoleMenuMapper.insert(roleMenu);
        }
    }

    private void checkCodeUnique(String code, Long excludeId) {
        Long count = sysRoleMapper.selectCount(
                Wrappers.<SysRole>lambdaQuery()
                        .eq(SysRole::getCode, code)
                        .ne(excludeId != null, SysRole::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException("角色編碼已存在");
        }
    }

    private RoleVO toVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setCode(role.getCode());
        vo.setSortNo(role.getSortNo());
        vo.setStatus(role.getStatus());
        vo.setRemark(role.getRemark());
        vo.setCreatedAt(role.getCreatedAt());
        return vo;
    }
}
