package com.novaboot.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.system.annotation.BusinessType;
import com.novaboot.module.system.annotation.OperLog;
import com.novaboot.module.system.dto.DictDataBody;
import com.novaboot.module.system.dto.DictDataQuery;
import com.novaboot.module.system.dto.DictDataVO;
import com.novaboot.module.system.dto.DictTypeBody;
import com.novaboot.module.system.dto.DictTypeQuery;
import com.novaboot.module.system.dto.DictTypeVO;
import com.novaboot.module.system.service.SysDictService;
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
 * 字典管理接口（類型 + 數據）
 */
@RestController
@RequestMapping("/api/system/dicts")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService sysDictService;

    // ---------------------------------- 字典類型 ----------------------------------

    @GetMapping("/types")
    @SaCheckPermission("system:dict:list")
    public Result<PageResult<DictTypeVO>> typePage(DictTypeQuery query) {
        return Result.ok(sysDictService.typePage(query));
    }

    @PostMapping("/types")
    @SaCheckPermission("system:dict:add")
    @OperLog(title = "字典管理", businessType = BusinessType.INSERT)
    public Result<Void> typeCreate(@Valid @RequestBody DictTypeBody body) {
        sysDictService.typeCreate(body);
        return Result.ok();
    }

    @PutMapping("/types/{id}")
    @SaCheckPermission("system:dict:edit")
    @OperLog(title = "字典管理", businessType = BusinessType.UPDATE)
    public Result<Void> typeUpdate(@PathVariable Long id, @Valid @RequestBody DictTypeBody body) {
        sysDictService.typeUpdate(id, body);
        return Result.ok();
    }

    @DeleteMapping("/types/{id}")
    @SaCheckPermission("system:dict:delete")
    @OperLog(title = "字典管理", businessType = BusinessType.DELETE)
    public Result<Void> typeDelete(@PathVariable Long id) {
        sysDictService.typeDelete(id);
        return Result.ok();
    }

    // ---------------------------------- 字典數據 ----------------------------------

    @GetMapping("/data")
    @SaCheckPermission("system:dict:list")
    public Result<PageResult<DictDataVO>> dataPage(DictDataQuery query) {
        return Result.ok(sysDictService.dataPage(query));
    }

    /**
     * 按類型取全部啟用字典項（僅需登入，下拉用）
     */
    @GetMapping("/data/type/{type}")
    public Result<List<DictDataVO>> listByType(@PathVariable String type) {
        return Result.ok(sysDictService.listByType(type));
    }

    @PostMapping("/data")
    @SaCheckPermission("system:dict:add")
    @OperLog(title = "字典管理", businessType = BusinessType.INSERT)
    public Result<Void> dataCreate(@Valid @RequestBody DictDataBody body) {
        sysDictService.dataCreate(body);
        return Result.ok();
    }

    @PutMapping("/data/{id}")
    @SaCheckPermission("system:dict:edit")
    @OperLog(title = "字典管理", businessType = BusinessType.UPDATE)
    public Result<Void> dataUpdate(@PathVariable Long id, @Valid @RequestBody DictDataBody body) {
        sysDictService.dataUpdate(id, body);
        return Result.ok();
    }

    @DeleteMapping("/data/{id}")
    @SaCheckPermission("system:dict:delete")
    @OperLog(title = "字典管理", businessType = BusinessType.DELETE)
    public Result<Void> dataDelete(@PathVariable Long id) {
        sysDictService.dataDelete(id);
        return Result.ok();
    }
}
