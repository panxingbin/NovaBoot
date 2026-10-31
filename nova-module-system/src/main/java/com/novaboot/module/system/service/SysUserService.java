package com.novaboot.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.datascope.DataScope;
import com.novaboot.module.system.datascope.DataScopeHolder;
import com.novaboot.module.system.datascope.DataScopeInfo;
import com.novaboot.module.system.dto.UserBody;
import com.novaboot.module.system.dto.UserQuery;
import com.novaboot.module.system.dto.UserVO;
import com.novaboot.module.system.entity.SysUser;
import com.novaboot.module.system.entity.SysUserPost;
import com.novaboot.module.system.entity.SysUserRole;
import com.novaboot.module.system.mapper.SysUserMapper;
import com.novaboot.module.system.mapper.SysUserPostMapper;
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
    private final SysUserPostMapper sysUserPostMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分頁查詢（啟用 @DataScope 資料權限：按角色數據範圍過濾可見用戶）
     */
    @DataScope
    public PageResult<UserVO> page(UserQuery query) {
        var wrapper = Wrappers.<SysUser>lambdaQuery()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(SysUser::getUsername, query.getKeyword())
                        .or().like(SysUser::getNickname, query.getKeyword()))
                .eq(query.getDeptId() != null, SysUser::getDeptId, query.getDeptId());
        applyDataScope(wrapper);
        wrapper.orderByDesc(SysUser::getId);
        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        List<Long> userIds = page.getRecords().stream().map(SysUser::getId).toList();
        Map<Long, List<Long>> roleIdsByUser = userIds.isEmpty() ? Map.of()
                : sysUserRoleMapper.selectList(
                                Wrappers.<SysUserRole>lambdaQuery().in(SysUserRole::getUserId, userIds))
                        .stream()
                        .collect(Collectors.groupingBy(SysUserRole::getUserId,
                                Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));
        Map<Long, List<Long>> postIdsByUser = userIds.isEmpty() ? Map.of()
                : sysUserPostMapper.selectList(
                                Wrappers.<SysUserPost>lambdaQuery().in(SysUserPost::getUserId, userIds))
                        .stream()
                        .collect(Collectors.groupingBy(SysUserPost::getUserId,
                                Collectors.mapping(SysUserPost::getPostId, Collectors.toList())));
        List<UserVO> records = page.getRecords().stream()
                .map(user -> toVO(user,
                        roleIdsByUser.getOrDefault(user.getId(), List.of()),
                        postIdsByUser.getOrDefault(user.getId(), List.of())))
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
        List<Long> postIds = sysUserPostMapper.selectList(
                        Wrappers.<SysUserPost>lambdaQuery().eq(SysUserPost::getUserId, id))
                .stream().map(SysUserPost::getPostId).toList();
        return toVO(user, roleIds, postIds);
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
        saveUserPosts(user.getId(), body.getPostIds());
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
        sysUserPostMapper.delete(Wrappers.<SysUserPost>lambdaQuery().eq(SysUserPost::getUserId, id));
        saveUserPosts(id, body.getPostIds());
    }

    @Transactional
    public void delete(Long id) {
        if (id == 1L) {
            throw new BusinessException("內置管理員不允許刪除");
        }
        sysUserMapper.deleteById(id);
        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
        sysUserPostMapper.delete(Wrappers.<SysUserPost>lambdaQuery().eq(SysUserPost::getUserId, id));
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

    /**
     * 將資料權限範圍（AOP 已放入 ThreadLocal）應用到查詢條件
     */
    private void applyDataScope(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser> wrapper) {
        DataScopeInfo ds = DataScopeHolder.get();
        if (ds == null) {
            return;
        }
        wrapper.and(w -> {
            if (!ds.deptIds().isEmpty() && ds.selfUserId() != null) {
                w.in(SysUser::getDeptId, ds.deptIds()).or().eq(SysUser::getId, ds.selfUserId());
            } else if (!ds.deptIds().isEmpty()) {
                w.in(SysUser::getDeptId, ds.deptIds());
            } else if (ds.selfUserId() != null) {
                w.eq(SysUser::getId, ds.selfUserId());
            } else {
                w.apply("1=0");
            }
        });
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

    private void saveUserPosts(Long userId, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return;
        }
        for (Long postId : postIds) {
            SysUserPost userPost = new SysUserPost();
            userPost.setUserId(userId);
            userPost.setPostId(postId);
            sysUserPostMapper.insert(userPost);
        }
    }

    private UserVO toVO(SysUser user, List<Long> roleIds, List<Long> postIds) {
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
        vo.setPostIds(postIds);
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
