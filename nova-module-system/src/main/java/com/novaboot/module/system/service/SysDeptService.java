package com.novaboot.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.dto.DeptBody;
import com.novaboot.module.system.dto.DeptVO;
import com.novaboot.module.system.entity.SysDept;
import com.novaboot.module.system.entity.SysUser;
import com.novaboot.module.system.mapper.SysDeptMapper;
import com.novaboot.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部門服務
 */
@Service
@RequiredArgsConstructor
public class SysDeptService {

    private final SysDeptMapper sysDeptMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 部門樹
     */
    public List<DeptVO> tree() {
        List<SysDept> all = sysDeptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
                .orderByAsc(SysDept::getSortNo));
        Map<Long, List<SysDept>> byParent = all.stream()
                .collect(Collectors.groupingBy(SysDept::getParentId));
        return assemble(byParent, 0L);
    }

    /**
     * 平鋪列表（管理用）
     */
    public List<DeptVO> list(String keyword) {
        return sysDeptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
                        .like(StringUtils.hasText(keyword), SysDept::getName, keyword)
                        .orderByAsc(SysDept::getParentId)
                        .orderByAsc(SysDept::getSortNo))
                .stream().map(d -> toVO(d, null)).toList();
    }

    @Transactional
    public void create(DeptBody body) {
        if (body.getParentId() != 0) {
            checkExists(body.getParentId());
        }
        SysDept dept = new SysDept();
        copyBody(dept, body);
        sysDeptMapper.insert(dept);
    }

    @Transactional
    public void update(Long id, DeptBody body) {
        SysDept dept = sysDeptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部門不存在");
        }
        if (id.equals(body.getParentId())) {
            throw new BusinessException("上級部門不能是自身");
        }
        if (isDescendant(body.getParentId(), id)) {
            throw new BusinessException("上級部門不能是自己的子部門");
        }
        copyBody(dept, body);
        sysDeptMapper.updateById(dept);
    }

    @Transactional
    public void delete(Long id) {
        if (id == 1L) {
            throw new BusinessException("內置根部門不允許刪除");
        }
        Long children = sysDeptMapper.selectCount(
                Wrappers.<SysDept>lambdaQuery().eq(SysDept::getParentId, id));
        if (children != null && children > 0) {
            throw new BusinessException("存在子部門，不允許刪除");
        }
        Long users = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getDeptId, id));
        if (users != null && users > 0) {
            throw new BusinessException("部門下存在用戶，不允許刪除");
        }
        sysDeptMapper.deleteById(id);
    }

    private void checkExists(Long id) {
        if (sysDeptMapper.selectById(id) == null) {
            throw new BusinessException("上級部門不存在");
        }
    }

    /**
     * 判斷 ancestorId 是否是 id 的祖先（含自身）
     */
    private boolean isDescendant(Long ancestorId, Long id) {
        Long cur = ancestorId;
        while (cur != null && cur != 0) {
            if (cur.equals(id)) {
                return true;
            }
            SysDept dept = sysDeptMapper.selectById(cur);
            cur = dept == null ? null : dept.getParentId();
        }
        return false;
    }

    private List<DeptVO> assemble(Map<Long, List<SysDept>> byParent, Long parentId) {
        List<DeptVO> result = new ArrayList<>();
        for (SysDept dept : byParent.getOrDefault(parentId, List.of())) {
            result.add(toVO(dept, assemble(byParent, dept.getId())));
        }
        return result;
    }

    private void copyBody(SysDept dept, DeptBody body) {
        dept.setParentId(body.getParentId());
        dept.setName(body.getName());
        dept.setSortNo(body.getSortNo() == null ? 0 : body.getSortNo());
        dept.setLeader(body.getLeader());
        dept.setPhone(body.getPhone());
        dept.setStatus(body.getStatus());
    }

    private DeptVO toVO(SysDept dept, List<DeptVO> children) {
        DeptVO vo = new DeptVO();
        vo.setId(dept.getId());
        vo.setParentId(dept.getParentId());
        vo.setName(dept.getName());
        vo.setSortNo(dept.getSortNo());
        vo.setLeader(dept.getLeader());
        vo.setPhone(dept.getPhone());
        vo.setStatus(dept.getStatus());
        vo.setCreatedAt(dept.getCreatedAt());
        vo.setChildren(children);
        return vo;
    }
}
