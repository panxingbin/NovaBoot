package com.novaboot.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.module.system.dto.LoginLogQuery;
import com.novaboot.module.system.entity.SysLoginLog;
import com.novaboot.module.system.mapper.SysLoginLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 登入日誌服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final SysLoginLogMapper sysLoginLogMapper;

    public PageResult<SysLoginLog> page(LoginLogQuery query) {
        Page<SysLoginLog> page = sysLoginLogMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                Wrappers.<SysLoginLog>lambdaQuery()
                        .like(StringUtils.hasText(query.getUsername()), SysLoginLog::getUsername, query.getUsername())
                        .eq(query.getStatus() != null, SysLoginLog::getStatus, query.getStatus())
                        .orderByDesc(SysLoginLog::getId));
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    @Async
    public void record(String username, String ip, Integer status, String msg) {
        try {
            SysLoginLog logEntity = new SysLoginLog();
            logEntity.setUsername(username);
            logEntity.setIp(ip);
            logEntity.setStatus(status);
            logEntity.setMsg(msg);
            sysLoginLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.warn("登入日誌落庫失敗: {}", e.getMessage());
        }
    }
}
