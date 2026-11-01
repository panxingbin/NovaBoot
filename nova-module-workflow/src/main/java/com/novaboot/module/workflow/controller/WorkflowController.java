package com.novaboot.module.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 工作流運行時接口（登入即可用；審批範圍由流程候選人/候選組控制）
 */
@RestController
@RequestMapping("/api/workflow")
@SaCheckLogin
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    public record ApplyBody(String formKey, Map<String, Object> data) {
    }

    public record CompleteBody(boolean approved, String comment) {
    }

    /** 發起申請（如請假單） */
    @PostMapping("/apply")
    public Result<String> apply(@RequestBody ApplyBody body) {
        if (body == null || body.formKey() == null || body.formKey().isBlank()) {
            return Result.fail("formKey 不能為空");
        }
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(workflowService.apply(userId, body.formKey(), body.data()));
    }

    /** 我的待辦 */
    @GetMapping("/todo")
    public Result<List<Map<String, Object>>> todo() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(workflowService.todoTasks(userId, StpUtil.getRoleList(userId)));
    }

    /** 審批（批准/駁回 + 意見） */
    @PostMapping("/tasks/{taskId}/complete")
    public Result<Void> complete(@PathVariable String taskId, @RequestBody CompleteBody body) {
        long userId = StpUtil.getLoginIdAsLong();
        workflowService.complete(userId, taskId, body.approved(), body.comment());
        return Result.ok();
    }

    /** 我的申請（進行中 + 已結束） */
    @GetMapping("/mine")
    public Result<List<Map<String, Object>>> mine() {
        return Result.ok(workflowService.myApplications(StpUtil.getLoginIdAsLong()));
    }

    /** 我的已辦 */
    @GetMapping("/done")
    public Result<List<Map<String, Object>>> done() {
        return Result.ok(workflowService.doneTasks(StpUtil.getLoginIdAsLong()));
    }
}
