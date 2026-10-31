package com.novaboot.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.system.annotation.BusinessType;
import com.novaboot.module.system.annotation.OperLog;
import com.novaboot.module.system.dto.LoginLogQuery;
import com.novaboot.module.system.dto.OnlineUserVO;
import com.novaboot.module.system.dto.OperLogQuery;
import com.novaboot.module.system.entity.SysLoginLog;
import com.novaboot.module.system.entity.SysOperLog;
import com.novaboot.module.system.service.LoginLogService;
import com.novaboot.module.system.service.OnlineUserService;
import com.novaboot.module.system.service.OperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系統監控接口：操作日誌、登入日誌、在線用戶
 */
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final OperLogService operLogService;
    private final LoginLogService loginLogService;
    private final OnlineUserService onlineUserService;

    @GetMapping("/operlogs")
    @SaCheckPermission("monitor:operlog:list")
    public Result<PageResult<SysOperLog>> operLogs(OperLogQuery query) {
        return Result.ok(operLogService.page(query));
    }

    @DeleteMapping("/operlogs/{id}")
    @SaCheckPermission("monitor:operlog:delete")
    @OperLog(title = "操作日誌", businessType = BusinessType.DELETE)
    public Result<Void> deleteOperLog(@PathVariable Long id) {
        operLogService.delete(id);
        return Result.ok();
    }

    @GetMapping("/loginlogs")
    @SaCheckPermission("monitor:loginlog:list")
    public Result<PageResult<SysLoginLog>> loginLogs(LoginLogQuery query) {
        return Result.ok(loginLogService.page(query));
    }

    @GetMapping("/online")
    @SaCheckPermission("monitor:online:list")
    public Result<PageResult<OnlineUserVO>> online(int pageNum, int pageSize, String keyword) {
        return Result.ok(onlineUserService.page(pageNum, pageSize, keyword));
    }

    @DeleteMapping("/online/{token}")
    @SaCheckPermission("monitor:online:forceLogout")
    @OperLog(title = "在線用戶", businessType = BusinessType.FORCE_LOGOUT)
    public Result<Void> forceLogout(@PathVariable String token) {
        onlineUserService.forceLogout(token);
        return Result.ok();
    }
}
