package com.novaboot.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.system.annotation.BusinessType;
import com.novaboot.module.system.annotation.OperLog;
import com.novaboot.module.system.dto.ConfigBody;
import com.novaboot.module.system.dto.ConfigQuery;
import com.novaboot.module.system.dto.ConfigVO;
import com.novaboot.module.system.service.SysConfigService;
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
 * 系統參數接口
 */
@RestController
@RequestMapping("/api/system/configs")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @GetMapping
    @SaCheckPermission("system:config:list")
    public Result<PageResult<ConfigVO>> page(ConfigQuery query) {
        return Result.ok(sysConfigService.page(query));
    }

    /**
     * 按鍵名取參數值（僅需登入）
     */
    @GetMapping("/key/{key}")
    public Result<String> byKey(@PathVariable String key) {
        return Result.ok(sysConfigService.valueOfKey(key));
    }

    @PostMapping
    @SaCheckPermission("system:config:add")
    @OperLog(title = "參數設置", businessType = BusinessType.INSERT)
    public Result<Void> create(@Valid @RequestBody ConfigBody body) {
        sysConfigService.create(body);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:config:edit")
    @OperLog(title = "參數設置", businessType = BusinessType.UPDATE)
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ConfigBody body) {
        sysConfigService.update(id, body);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:config:delete")
    @OperLog(title = "參數設置", businessType = BusinessType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        sysConfigService.delete(id);
        return Result.ok();
    }
}
