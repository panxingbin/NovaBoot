package com.novaboot.module.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.workflow.entity.FlowFormDef;
import com.novaboot.module.workflow.service.FlowFormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 可配置表單管理接口
 */
@RestController
@RequestMapping("/api/workflow/forms")
@RequiredArgsConstructor
public class FlowFormController {

    private final FlowFormService formService;

    /** 表單定義分頁（管理） */
    @GetMapping
    @SaCheckPermission("workflow:form")
    public Result<PageResult<FlowFormDef>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(required = false) String keyword) {
        return Result.ok(formService.page(pageNum, pageSize, keyword));
    }

    /** 全部表單（發起申請頁的下拉） */
    @GetMapping("/all")
    public Result<List<FlowFormDef>> all() {
        return Result.ok(formService.listAll());
    }

    /** 按鍵取 schema（動態表單渲染用） */
    @GetMapping("/key/{formKey}")
    public Result<FlowFormDef> byKey(@PathVariable String formKey) {
        return Result.ok(formService.getByKey(formKey));
    }

    @PostMapping
    @SaCheckPermission("workflow:form:add")
    public Result<Void> create(@RequestBody FlowFormDef def) {
        formService.create(def);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("workflow:form:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody FlowFormDef def) {
        formService.update(id, def);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("workflow:form:delete")
    public Result<Void> delete(@PathVariable Long id) {
        formService.delete(id);
        return Result.ok();
    }
}
