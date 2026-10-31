package com.novaboot.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.system.dto.MenuBody;
import com.novaboot.module.system.dto.MenuVO;
import com.novaboot.module.system.service.SysMenuService;
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
 * 選單管理接口
 */
@RestController
@RequestMapping("/api/system/menus")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    /**
     * 路由用選單樹（僅需登入）
     */
    @GetMapping("/tree")
    public Result<List<MenuVO>> tree() {
        return Result.ok(sysMenuService.tree());
    }

    @GetMapping
    @SaCheckPermission("system:menu:list")
    public Result<List<MenuVO>> list(String keyword) {
        return Result.ok(sysMenuService.list(keyword));
    }

    @PostMapping
    @SaCheckPermission("system:menu:add")
    public Result<Void> create(@Valid @RequestBody MenuBody body) {
        sysMenuService.create(body);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:menu:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MenuBody body) {
        sysMenuService.update(id, body);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:menu:delete")
    public Result<Void> delete(@PathVariable Long id) {
        sysMenuService.delete(id);
        return Result.ok();
    }
}
