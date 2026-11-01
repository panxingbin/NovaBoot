package com.novaboot.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.dto.RoleBody;
import com.novaboot.module.system.dto.RoleQuery;
import com.novaboot.module.system.dto.RoleVO;
import com.novaboot.module.system.entity.SysRole;
import com.novaboot.module.system.entity.SysRoleDept;
import com.novaboot.module.system.entity.SysRoleMenu;
import com.novaboot.module.system.entity.SysUserRole;
import com.novaboot.module.system.enums.DataScopeType;
import com.novaboot.module.system.mapper.SysRoleDeptMapper;
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
    private final SysRoleDeptMapper sysRoleDeptMapper;

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
        copyBody(role, body);
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
        copyBody(role, body);
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
        sysRoleDeptMapper.delete(Wrappers.<SysRoleDept>lambdaQuery().eq(SysRoleDept::getRoleId, id));
    }

    public List<Long> getMenuIds(Long roleId) {
        return sysRoleMenuMapper.selectList(
                        Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).toList();
    }

    public List<Long> getDeptIds(Long roleId) {
        return sysRoleDeptMapper.selectList(
                        Wrappers.<SysRoleDept>lambdaQuery().eq(SysRoleDept::getRoleId, roleId))
                .stream().map(SysRoleDept::getDeptId).toList();
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
        List<SysRoleMenu> relations = menuIds.stream().map(menuId -> {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            return roleMenu;
        }).toList();
        Db.saveBatch(relations);
    }

    @Transactional
    public void assignDepts(Long roleId, List<Long> deptIds) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        sysRoleDeptMapper.delete(Wrappers.<SysRoleDept>lambdaQuery().eq(SysRoleDept::getRoleId, roleId));
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }
        List<SysRoleDept> relations = deptIds.stream().map(deptId -> {
            SysRoleDept roleDept = new SysRoleDept();
            roleDept.setRoleId(roleId);
            roleDept.setDeptId(deptId);
            return roleDept;
        }).toList();
        Db.saveBatch(relations);
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

    private void copyBody(SysRole role, RoleBody body) {
        role.setName(body.getName());
        role.setCode(body.getCode());
        role.setSortNo(body.getSortNo() == null ? 0 : body.getSortNo());
        role.setStatus(body.getStatus());
        role.setDataScope(body.getDataScope() == null ? DataScopeType.ALL.getValue() : body.getDataScope());
        role.setRemark(body.getRemark());
    }

    private RoleVO toVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setCode(role.getCode());
        vo.setSortNo(role.getSortNo());
        vo.setStatus(role.getStatus());
        vo.setDataScope(role.getDataScope());
        vo.setRemark(role.getRemark());
        vo.setCreatedAt(role.getCreatedAt());
        return vo;
    }
}
