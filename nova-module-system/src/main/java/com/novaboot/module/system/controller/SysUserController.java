package com.novaboot.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.system.dto.ResetPasswordBody;
import com.novaboot.module.system.dto.UserBody;
import com.novaboot.module.system.dto.UserQuery;
import com.novaboot.module.system.dto.UserVO;
import com.novaboot.module.system.service.SysUserService;
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
 * 用戶管理接口
 */
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping
    @SaCheckPermission("system:user:list")
    public Result<PageResult<UserVO>> page(UserQuery query) {
        return Result.ok(sysUserService.page(query));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:user:list")
    public Result<UserVO> get(@PathVariable Long id) {
        return Result.ok(sysUserService.get(id));
    }

    @PostMapping
    @SaCheckPermission("system:user:add")
    public Result<Void> create(@RequestBody UserBody body) {
        sysUserService.create(body);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:user:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserBody body) {
        sysUserService.update(id, body);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:delete")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/password")
    @SaCheckPermission("system:user:resetPwd")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordBody body) {
        sysUserService.resetPassword(id, body.getPassword());
        return Result.ok();
    }
}
