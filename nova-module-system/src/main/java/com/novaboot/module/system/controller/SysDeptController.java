package com.novaboot.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.system.annotation.BusinessType;
import com.novaboot.module.system.annotation.OperLog;
import com.novaboot.module.system.dto.DeptBody;
import com.novaboot.module.system.dto.DeptVO;
import com.novaboot.module.system.service.SysDeptService;
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

import java.util.List;

/**
 * 部門管理接口
 */
@RestController
@RequestMapping("/api/system/depts")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;

    /**
     * 部門樹（僅需登入，供用戶表單/角色數據範圍選擇）
     */
    @GetMapping("/tree")
    public Result<List<DeptVO>> tree() {
        return Result.ok(sysDeptService.tree());
    }

    @GetMapping
    @SaCheckPermission("system:dept:list")
    public Result<List<DeptVO>> list(String keyword) {
        return Result.ok(sysDeptService.list(keyword));
    }

    @PostMapping
    @SaCheckPermission("system:dept:add")
    @OperLog(title = "部門管理", businessType = BusinessType.INSERT)
    public Result<Void> create(@Valid @RequestBody DeptBody body) {
        sysDeptService.create(body);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:dept:edit")
    @OperLog(title = "部門管理", businessType = BusinessType.UPDATE)
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DeptBody body) {
        sysDeptService.update(id, body);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:dept:delete")
    @OperLog(title = "部門管理", businessType = BusinessType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        sysDeptService.delete(id);
        return Result.ok();
    }
}
