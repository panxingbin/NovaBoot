package com.novaboot.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.dto.UserBody;
import com.novaboot.module.system.dto.UserQuery;
import com.novaboot.module.system.dto.UserVO;
import com.novaboot.module.system.entity.SysUser;
import com.novaboot.module.system.entity.SysUserRole;
import com.novaboot.module.system.mapper.SysUserMapper;
import com.novaboot.module.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用戶服務
 */
@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<UserVO> page(UserQuery query) {
        Page<SysUser> page = sysUserMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                Wrappers.<SysUser>lambdaQuery()
                        .and(StringUtils.hasText(query.getKeyword()), w -> w
                                .like(SysUser::getUsername, query.getKeyword())
                                .or().like(SysUser::getNickname, query.getKeyword()))
                        .orderByDesc(SysUser::getId));
        List<Long> userIds = page.getRecords().stream().map(SysUser::getId).toList();
        Map<Long, List<Long>> roleIdsByUser = userIds.isEmpty() ? Map.of()
                : sysUserRoleMapper.selectList(
                                Wrappers.<SysUserRole>lambdaQuery().in(SysUserRole::getUserId, userIds))
                        .stream()
                        .collect(Collectors.groupingBy(SysUserRole::getUserId,
                                Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));
        List<UserVO> records = page.getRecords().stream()
                .map(user -> toVO(user, roleIdsByUser.getOrDefault(user.getId(), List.of())))
                .toList();
        return PageResult.of(page.getTotal(), records);
    }

    public UserVO get(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用戶不存在");
        }
        List<Long> roleIds = sysUserRoleMapper.selectList(
                        Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id))
                .stream().map(SysUserRole::getRoleId).toList();
        return toVO(user, roleIds);
    }

    @Transactional
    public void create(UserBody body) {
        if (!StringUtils.hasText(body.getUsername())) {
            throw new BusinessException("用戶名不能為空");
        }
        if (!StringUtils.hasText(body.getPassword())) {
            throw new BusinessException("密碼不能為空");
        }
        Long count = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, body.getUsername().trim()));
        if (count != null && count > 0) {
            throw new BusinessException("用戶名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(body.getUsername().trim());
        user.setPassword(passwordEncoder.encode(body.getPassword()));
        copyBody(user, body);
        sysUserMapper.insert(user);
        saveUserRoles(user.getId(), body.getRoleIds());
    }

    @Transactional
    public void update(Long id, UserBody body) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用戶不存在");
        }
        if (id == 1L && body.getStatus() != null && body.getStatus() != 1) {
            throw new BusinessException("內置管理員不允許停用");
        }
        copyBody(user, body);
        sysUserMapper.updateById(user);
        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
        saveUserRoles(id, body.getRoleIds());
    }

    @Transactional
    public void delete(Long id) {
        if (id == 1L) {
            throw new BusinessException("內置管理員不允許刪除");
        }
        sysUserMapper.deleteById(id);
        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
    }

    @Transactional
    public void resetPassword(Long id, String password) {
        if (!StringUtils.hasText(password)) {
            throw new BusinessException("密碼不能為空");
        }
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用戶不存在");
        }
        user.setPassword(passwordEncoder.encode(password));
        sysUserMapper.updateById(user);
    }

    private void copyBody(SysUser user, UserBody body) {
        user.setNickname(body.getNickname());
        user.setDeptId(body.getDeptId());
        user.setEmail(body.getEmail());
        user.setPhone(body.getPhone());
        user.setStatus(body.getStatus());
        user.setRemark(body.getRemark());
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        }
    }

    private UserVO toVO(SysUser user, List<Long> roleIds) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setDeptId(user.getDeptId());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setRemark(user.getRemark());
        vo.setRoleIds(roleIds);
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
