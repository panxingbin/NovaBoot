package com.novaboot.module.workflow.service;

import cn.hutool.json.JSONUtil;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.workflow.entity.FlowFormDef;
import lombok.RequiredArgsConstructor;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 工作流運行時服務：發起申請、待辦查詢、審批、我的申請/已辦
 * 身份映射：Flowable userId = NovaBoot 用戶 ID（字符串）；候選組 = 角色編碼
 */
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final FlowFormService formService;

    /**
     * 發起申請：formKey 即流程鍵（如 leave），數據按表單 schema 校驗後作為流程變量
     */
    public String apply(long userId, String formKey, Map<String, Object> data) {
        FlowFormDef def = formService.requireByKey(formKey);
        Map<String, Object> cleaned = formService.validateData(def.getSchemaJson(), data);
        Authentication.setAuthenticatedUserId(String.valueOf(userId));
        try {
            Map<String, Object> variables = new HashMap<>(cleaned);
            variables.put("formKey", formKey);
            variables.put("dataJson", JSONUtil.toJsonStr(cleaned));
            variables.put("applicant", String.valueOf(userId));
            ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                    formKey, UUID.randomUUID().toString(), variables);
            return instance.getId();
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }
    }

    /**
     * 我的待辦：我是受理人/候選人，或任務候選組與我的任一角色匹配
     */
    public List<Map<String, Object>> todoTasks(long userId, List<String> roleCodes) {
        List<Task> tasks = taskService.createTaskQuery()
                .or()
                .taskCandidateOrAssigned(String.valueOf(userId))
                .taskCandidateGroupIn(roleCodes == null ? List.of() : roleCodes)
                .endOr()
                .includeProcessVariables()
                .orderByTaskCreateTime().desc()
                .list();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> row = new HashMap<>();
            row.put("taskId", task.getId());
            row.put("taskName", task.getName());
            row.put("createTime", task.getCreateTime());
            row.put("processInstanceId", task.getProcessInstanceId());
            row.put("formKey", task.getProcessVariables().get("formKey"));
            String dataJson = (String) task.getProcessVariables().get("dataJson");
            row.put("formData", dataJson == null ? Map.of() : JSONUtil.parseObj(dataJson));
            result.add(row);
        }
        return result;
    }

    /**
     * 審批：受理人/候選人完成任務；approved 決定流程走向（批准/駁回）
     */
    public void complete(long userId, String taskId, boolean approved, String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任務不存在或已處理");
        }
        if (task.getAssignee() == null) {
            taskService.claim(taskId, String.valueOf(userId));
        } else if (!task.getAssignee().equals(String.valueOf(userId))) {
            // 已被他人受理：仍允許同組候選人處理，但記錄為受理人
            taskService.unclaim(taskId);
            taskService.claim(taskId, String.valueOf(userId));
        }
        if (comment != null && !comment.isBlank()) {
            taskService.addComment(taskId, task.getProcessInstanceId(), comment);
        }
        taskService.setVariable(task.getId(), "approved", approved);
        taskService.complete(taskId);
    }

    /**
     * 我的申請：進行中 + 已結束（含審批結論與表單數據）
     */
    public List<Map<String, Object>> myApplications(long userId) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                .startedBy(String.valueOf(userId))
                .orderByProcessInstanceStartTime().desc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (HistoricProcessInstance instance : query.includeProcessVariables().list()) {
            Map<String, Object> row = new HashMap<>();
            row.put("processInstanceId", instance.getId());
            row.put("formKey", instance.getProcessDefinitionKey());
            row.put("startTime", instance.getStartTime());
            row.put("endTime", instance.getEndTime());
            row.put("running", instance.getEndTime() == null);
            Object approved = instance.getProcessVariables().get("approved");
            row.put("approved", approved);
            row.put("status", instance.getEndTime() == null
                    ? "審批中"
                    : Boolean.TRUE.equals(approved) ? "已批准" : "已駁回");
            String dataJson = (String) instance.getProcessVariables().get("dataJson");
            row.put("formData", dataJson == null ? Map.of() : JSONUtil.parseObj(dataJson));
            result.add(row);
        }
        return result;
    }

    /**
     * 我的已辦
     */
    public List<Map<String, Object>> doneTasks(long userId) {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(String.valueOf(userId))
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .list();
        List<Map<String, Object>> result = new ArrayList<>();
        for (HistoricTaskInstance task : tasks) {
            Map<String, Object> row = new HashMap<>();
            row.put("taskId", task.getId());
            row.put("taskName", task.getName());
            row.put("processInstanceId", task.getProcessInstanceId());
            row.put("startTime", task.getStartTime());
            row.put("endTime", task.getEndTime());
            List<String> comments = taskService.getTaskComments(task.getId())
                    .stream().map(c -> c.getFullMessage()).toList();
            row.put("comments", comments);
            result.add(row);
        }
        return result;
    }
}
