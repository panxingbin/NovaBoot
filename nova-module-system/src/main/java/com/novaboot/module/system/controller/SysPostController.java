package com.novaboot.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.system.annotation.BusinessType;
import com.novaboot.module.system.annotation.OperLog;
import com.novaboot.module.system.dto.PostBody;
import com.novaboot.module.system.dto.PostQuery;
import com.novaboot.module.system.dto.PostVO;
import com.novaboot.module.system.service.SysPostService;
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
 * 崗位管理接口
 */
@RestController
@RequestMapping("/api/system/posts")
@RequiredArgsConstructor
public class SysPostController {

    private final SysPostService sysPostService;

    @GetMapping
    @SaCheckPermission("system:post:list")
    public Result<PageResult<PostVO>> page(PostQuery query) {
        return Result.ok(sysPostService.page(query));
    }

    /**
     * 全部啟用崗位（僅需登入，供用戶表單下拉）
     */
    @GetMapping("/all")
    public Result<List<PostVO>> all() {
        return Result.ok(sysPostService.all());
    }

    @PostMapping
    @SaCheckPermission("system:post:add")
    @OperLog(title = "崗位管理", businessType = BusinessType.INSERT)
    public Result<Void> create(@Valid @RequestBody PostBody body) {
        sysPostService.create(body);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:post:edit")
    @OperLog(title = "崗位管理", businessType = BusinessType.UPDATE)
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PostBody body) {
        sysPostService.update(id, body);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:post:delete")
    @OperLog(title = "崗位管理", businessType = BusinessType.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        sysPostService.delete(id);
        return Result.ok();
    }
}
