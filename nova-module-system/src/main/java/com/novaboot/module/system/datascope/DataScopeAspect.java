package com.novaboot.module.system.datascope;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.novaboot.module.system.auth.AdminRoleChecker;
import com.novaboot.module.system.entity.SysDept;
import com.novaboot.module.system.entity.SysRole;
import com.novaboot.module.system.entity.SysRoleDept;
import com.novaboot.module.system.entity.SysUser;
import com.novaboot.module.system.entity.SysUserRole;
import com.novaboot.module.system.enums.DataScopeType;
import com.novaboot.module.system.mapper.SysDeptMapper;
import com.novaboot.module.system.mapper.SysRoleDeptMapper;
import com.novaboot.module.system.mapper.SysRoleMapper;
import com.novaboot.module.system.mapper.SysUserMapper;
import com.novaboot.module.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 資料權限切面：計算當前用戶全部角色的數據範圍並集，放入 DataScopeHolder
 * （超級管理員不做任何過濾）
 */
@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeAspect {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleDeptMapper sysRoleDeptMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysDeptMapper sysDeptMapper;
    private final AdminRoleChecker adminRoleChecker;

    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint joinPoint, DataScope dataScope) throws Throwable {
        try {
            DataScopeInfo info = compute();
            if (info != null) {
                DataScopeHolder.set(info);
            }
            return joinPoint.proceed();
        } finally {
            DataScopeHolder.clear();
        }
    }

    /**
     * 返回 null 表示不需要過濾（超級管理員）；否則返回過濾條件
     */
    private DataScopeInfo compute() {
        long userId = StpUtil.getLoginIdAsLong();
        List<Long> roleIds = sysUserRoleMapper.selectList(
                        Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return new DataScopeInfo(Collections.emptyList(), null);
        }
        if (adminRoleChecker.hasAdminRole(roleIds)) {
            return null;
        }
        Set<Long> deptIds = new LinkedHashSet<>();
        Long selfUserId = null;
        SysUser user = sysUserMapper.selectById(userId);
        Long ownDeptId = user == null ? null : user.getDeptId();

        for (SysRole role : sysRoleMapper.selectList(Wrappers.<SysRole>lambdaQuery().in(SysRole::getId, roleIds))) {
            // 並集語義：任一角色為「全部」即不過濾
            switch (DataScopeType.of(role.getDataScope())) {
                case ALL -> {
                    return null;
                }
                case CUSTOM_DEPT -> deptIds.addAll(sysRoleDeptMapper.selectList(
                                Wrappers.<SysRoleDept>lambdaQuery().eq(SysRoleDept::getRoleId, role.getId()))
                        .stream().map(SysRoleDept::getDeptId).toList());
                case DEPT -> {
                    if (ownDeptId != null) {
                        deptIds.add(ownDeptId);
                    }
                }
                case DEPT_AND_CHILD -> {
                    if (ownDeptId != null) {
                        deptIds.add(ownDeptId);
                        deptIds.addAll(descendantsOf(ownDeptId));
                    }
                }
                case SELF -> selfUserId = userId;
            }
        }
        return new DataScopeInfo(new ArrayList<>(deptIds), selfUserId);
    }

    /**
     * 廣度優先收集某部門的全部子孫部門
     */
    private List<Long> descendantsOf(Long deptId) {
        List<SysDept> all = sysDeptMapper.selectList(Wrappers.emptyWrapper());
        Map<Long, List<Long>> childrenByParent = all.stream()
                .collect(Collectors.groupingBy(SysDept::getParentId,
                        Collectors.mapping(SysDept::getId, Collectors.toList())));
        List<Long> result = new ArrayList<>();
        List<Long> frontier = new ArrayList<>();
        frontier.add(deptId);
        while (!frontier.isEmpty()) {
            List<Long> next = new ArrayList<>();
            for (Long id : frontier) {
                for (Long child : childrenByParent.getOrDefault(id, List.of())) {
                    if (!result.contains(child)) {
                        result.add(child);
                        next.add(child);
                    }
                }
            }
            frontier = next;
        }
        return result;
    }
}
