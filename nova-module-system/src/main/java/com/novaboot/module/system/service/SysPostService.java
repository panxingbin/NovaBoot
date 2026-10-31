package com.novaboot.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.dto.PostBody;
import com.novaboot.module.system.dto.PostQuery;
import com.novaboot.module.system.dto.PostVO;
import com.novaboot.module.system.entity.SysPost;
import com.novaboot.module.system.entity.SysUserPost;
import com.novaboot.module.system.mapper.SysPostMapper;
import com.novaboot.module.system.mapper.SysUserPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 崗位服務
 */
@Service
@RequiredArgsConstructor
public class SysPostService {

    private final SysPostMapper sysPostMapper;
    private final SysUserPostMapper sysUserPostMapper;

    public PageResult<PostVO> page(PostQuery query) {
        Page<SysPost> page = sysPostMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                Wrappers.<SysPost>lambdaQuery()
                        .and(StringUtils.hasText(query.getKeyword()), w -> w
                                .like(SysPost::getName, query.getKeyword())
                                .or().like(SysPost::getCode, query.getKeyword()))
                        .orderByAsc(SysPost::getSortNo));
        List<PostVO> records = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), records);
    }

    /**
     * 全部啟用崗位（用戶表單下拉）
     */
    public List<PostVO> all() {
        return sysPostMapper.selectList(Wrappers.<SysPost>lambdaQuery()
                        .eq(SysPost::getStatus, 1)
                        .orderByAsc(SysPost::getSortNo))
                .stream().map(this::toVO).toList();
    }

    @Transactional
    public void create(PostBody body) {
        checkCodeUnique(body.getCode(), null);
        SysPost post = new SysPost();
        copyBody(post, body);
        sysPostMapper.insert(post);
    }

    @Transactional
    public void update(Long id, PostBody body) {
        SysPost post = sysPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("崗位不存在");
        }
        checkCodeUnique(body.getCode(), id);
        copyBody(post, body);
        sysPostMapper.updateById(post);
    }

    @Transactional
    public void delete(Long id) {
        Long used = sysUserPostMapper.selectCount(
                Wrappers.<SysUserPost>lambdaQuery().eq(SysUserPost::getPostId, id));
        if (used != null && used > 0) {
            throw new BusinessException("崗位已分配給用戶，不允許刪除");
        }
        sysPostMapper.deleteById(id);
    }

    private void checkCodeUnique(String code, Long excludeId) {
        Long count = sysPostMapper.selectCount(
                Wrappers.<SysPost>lambdaQuery()
                        .eq(SysPost::getCode, code)
                        .ne(excludeId != null, SysPost::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException("崗位編碼已存在");
        }
    }

    private void copyBody(SysPost post, PostBody body) {
        post.setCode(body.getCode());
        post.setName(body.getName());
        post.setSortNo(body.getSortNo() == null ? 0 : body.getSortNo());
        post.setStatus(body.getStatus());
        post.setRemark(body.getRemark());
    }

    private PostVO toVO(SysPost post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setCode(post.getCode());
        vo.setName(post.getName());
        vo.setSortNo(post.getSortNo());
        vo.setStatus(post.getStatus());
        vo.setRemark(post.getRemark());
        vo.setCreatedAt(post.getCreatedAt());
        return vo;
    }
}
