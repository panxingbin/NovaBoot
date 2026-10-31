package com.novaboot.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.dto.NoticeBody;
import com.novaboot.module.system.dto.NoticeQuery;
import com.novaboot.module.system.dto.NoticeVO;
import com.novaboot.module.system.entity.SysNotice;
import com.novaboot.module.system.mapper.SysNoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 通知公告服務
 */
@Service
@RequiredArgsConstructor
public class SysNoticeService {

    private final SysNoticeMapper sysNoticeMapper;

    public PageResult<NoticeVO> page(NoticeQuery query) {
        Page<SysNotice> page = sysNoticeMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                Wrappers.<SysNotice>lambdaQuery()
                        .like(StringUtils.hasText(query.getKeyword()), SysNotice::getTitle, query.getKeyword())
                        .orderByDesc(SysNotice::getId));
        List<NoticeVO> records = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), records);
    }

    public NoticeVO get(Long id) {
        SysNotice notice = sysNoticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        return toVO(notice);
    }

    @Transactional
    public void create(NoticeBody body) {
        SysNotice notice = new SysNotice();
        copyBody(notice, body);
        sysNoticeMapper.insert(notice);
    }

    @Transactional
    public void update(Long id, NoticeBody body) {
        SysNotice notice = sysNoticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        copyBody(notice, body);
        sysNoticeMapper.updateById(notice);
    }

    @Transactional
    public void delete(Long id) {
        sysNoticeMapper.deleteById(id);
    }

    private void copyBody(SysNotice notice, NoticeBody body) {
        notice.setTitle(body.getTitle());
        notice.setType(body.getType());
        notice.setContent(body.getContent());
        notice.setStatus(body.getStatus());
        notice.setRemark(body.getRemark());
    }

    private NoticeVO toVO(SysNotice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setType(notice.getType());
        vo.setContent(notice.getContent());
        vo.setStatus(notice.getStatus());
        vo.setRemark(notice.getRemark());
        vo.setCreatedAt(notice.getCreatedAt());
        return vo;
    }
}
