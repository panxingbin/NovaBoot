package com.novaboot.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.system.dto.RoleBody;
import com.novaboot.module.system.dto.RoleMenuBody;
import com.novaboot.module.system.dto.RoleQuery;
import com.novaboot.module.system.dto.RoleVO;
import com.novaboot.module.system.service.SysRoleService;
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
 * 角色管理接口
 */
@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @GetMapping
    @SaCheckPermission("system:role:list")
    public Result<PageResult<RoleVO>> page(RoleQuery query) {
        return Result.ok(sysRoleService.page(query));
    }

    @GetMapping("/{id}/menuIds")
    @SaCheckPermission("system:role:list")
    public Result<List<Long>> menuIds(@PathVariable Long id) {
        return Result.ok(sysRoleService.getMenuIds(id));
    }

    @PostMapping
    @SaCheckPermission("system:role:add")
    public Result<Void> create(@Valid @RequestBody RoleBody body) {
        sysRoleService.create(body);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:role:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleBody body) {
        sysRoleService.update(id, body);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:delete")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/menus")
    @SaCheckPermission("system:role:edit")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody RoleMenuBody body) {
        sysRoleService.assignMenus(id, body.getMenuIds());
        return Result.ok();
    }
}
