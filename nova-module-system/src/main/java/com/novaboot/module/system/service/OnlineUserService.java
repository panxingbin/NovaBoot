package com.novaboot.module.system.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaTerminalInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.dto.OnlineUserVO;
import com.novaboot.module.system.entity.SysUser;
import com.novaboot.module.system.mapper.SysUserMapper;
import com.novaboot.module.system.service.OnlineUserRegistry.OnlineEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 在線用戶服務：基於本地在線註冊表枚舉，查詢時校驗 token 存活以剔除過期會話
 */
@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final SysUserMapper sysUserMapper;
    private final OnlineUserRegistry onlineUserRegistry;

    public PageResult<OnlineUserVO> page(int pageNum, int pageSize, String keyword) {
        List<OnlineUserVO> all = new ArrayList<>();
        for (Map.Entry<String, OnlineEntry> entry : onlineUserRegistry.snapshot().entrySet()) {
            String token = entry.getKey();
            OnlineEntry online = entry.getValue();
            // token 已過期/被登出則自動剔除
            if (StpUtil.getLoginIdByToken(token) == null) {
                onlineUserRegistry.remove(token);
                continue;
            }
            SysUser user = sysUserMapper.selectById(online.loginId());
            if (user == null) {
                continue;
            }
            if (StringUtils.hasText(keyword)
                    && !user.getUsername().contains(keyword)
                    && !user.getNickname().contains(keyword)) {
                continue;
            }
            OnlineUserVO vo = new OnlineUserVO();
            vo.setToken(token);
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setDevice(deviceOf(online.loginId(), token));
            vo.setLoginTime(Instant.ofEpochMilli(online.loginTime()).toString());
            all.add(vo);
        }
        all.sort(Comparator.comparing(OnlineUserVO::getLoginTime).reversed());
        int total = all.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        return PageResult.of(total, new ArrayList<>(all.subList(from, to)));
    }

    public void forceLogout(String token) {
        Object loginId = StpUtil.getLoginIdByToken(token);
        if (loginId == null) {
            throw new BusinessException("會話不存在或已過期");
        }
        StpUtil.logoutByTokenValue(token);
        onlineUserRegistry.remove(token);
    }

    /**
     * 嘗試從 Sa-Token 會話終端列表取設備類型，取不到返回 null
     */
    private String deviceOf(long loginId, String token) {
        try {
            SaSession session = StpUtil.getSessionByLoginId(loginId, false);
            if (session != null) {
                for (SaTerminalInfo terminal : session.getTerminalList()) {
                    if (token.equals(terminal.getTokenValue())) {
                        return terminal.getDeviceType();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
