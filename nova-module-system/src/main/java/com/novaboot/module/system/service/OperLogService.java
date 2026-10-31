package com.novaboot.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.module.system.dto.OperLogQuery;
import com.novaboot.module.system.entity.SysOperLog;
import com.novaboot.module.system.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日誌服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperLogService {

    private final SysOperLogMapper sysOperLogMapper;

    public PageResult<SysOperLog> page(OperLogQuery query) {
        Page<SysOperLog> page = sysOperLogMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                Wrappers.<SysOperLog>lambdaQuery()
                        .like(StringUtils.hasText(query.getTitle()), SysOperLog::getTitle, query.getTitle())
                        .like(StringUtils.hasText(query.getUsername()), SysOperLog::getOperName, query.getUsername())
                        .eq(query.getStatus() != null, SysOperLog::getStatus, query.getStatus())
                        .orderByDesc(SysOperLog::getId));
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    public void delete(Long id) {
        sysOperLogMapper.deleteById(id);
    }

    @Async
    public void record(SysOperLog operLog) {
        try {
            sysOperLogMapper.insert(operLog);
        } catch (Exception e) {
            log.warn("操作日誌落庫失敗: {}", e.getMessage());
        }
    }
}
