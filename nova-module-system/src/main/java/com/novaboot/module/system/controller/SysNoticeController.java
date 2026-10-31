package com.novaboot.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.system.annotation.BusinessType;
import com.novaboot.module.system.annotation.OperLog;
import com.novaboot.module.system.dto.NoticeBody;
import com.novaboot.module.system.dto.NoticeQuery;
import com.novaboot.module.system.dto.NoticeVO;
import com.novaboot.module.system.service.SysNoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知公告接口
 */
@RestController
@RequestMapping("/api/system/notices")
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeService sysNoticeService;

    @GetMapping
    @SaCheckPermission("system:notice:list")
    public Result<PageResult<NoticeVO>> page(NoticeQuery query) {
        return Result.ok(sysNoticeService.page(query));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:notice:list")
    public Result<NoticeVO> get(@PathVariable Long id) {
        return Result.ok(sysNoticeService.get(id));
    }

    @PostMapping
    @SaCheckPermission("system:notice:add")
    @OperLog(title = "通知公告", businessType = BusinessType.INSERT)
    public Result<Void> create(@Valid @RequestBody NoticeBody body) {
        sysNoticeService.create(body);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:notice:edit")
    @OperLog(title = "通知公告", businessType = BusinessType.UPDATE)
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody NoticeBody body) {
        sysNoticeService.update(id, body);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:notice:delete")
    @OperLog(title = "通知公告", businessType = BusinessType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        sysNoticeService.delete(id);
        return Result.ok();
    }
}
