package com.novaboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工作流鏈路集成測試（mysql profile：Flowable 在真實庫自動建 ACT_ 表並部署 leave 流程）
 * 鏈路：登入 → 提交請假單 → 待辦出現 → 批准 → 我的申請顯示已批准
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "nova.captcha.enabled=false")
@ActiveProfiles("mysql")
class WorkflowIntegrationTest {

    private static final Pattern TOKEN_FIELD = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void leaveApprovalChain() {
        // 1. 登入
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> login = restTemplate.postForEntity(url("/api/auth/login"),
                new HttpEntity<>("{\"username\":\"admin\",\"password\":\"admin123\"}", loginHeaders), String.class);
        String loginBody = login.getBody();
        assertTrue(loginBody.contains("\"code\":0"), loginBody);
        Matcher matcher = TOKEN_FIELD.matcher(loginBody);
        assertTrue(matcher.find(), "應返回 token");
        HttpHeaders auth = authHeaders(matcher.group(1));

        // 2. 提交請假單（leave 表單已由 V4 遷移種子）
        HttpHeaders json = new HttpHeaders();
        json.setContentType(MediaType.APPLICATION_JSON);
        json.put(HttpHeaders.AUTHORIZATION, auth.get(HttpHeaders.AUTHORIZATION));
        String applyBody = "{\"formKey\":\"leave\",\"data\":{\"leaveType\":\"年假\",\"startDate\":\"2026-12-01\","
                + "\"days\":2,\"reason\":\"工作流集成測試\"}}";
        ResponseEntity<String> apply = restTemplate.exchange(url("/api/workflow/apply"),
                HttpMethod.POST, new HttpEntity<>(applyBody, json), String.class);
        String applyResp = apply.getBody();
        assertTrue(applyResp.contains("\"code\":0"), applyResp);
        String instanceId = extractJsonStringField(applyResp, "data");
        assertNotNull(instanceId, "應返回流程實例 ID");

        // 3. 待辦中出現主管審批任務（admin 角色為候選組）
        ResponseEntity<String> todo = restTemplate.exchange(url("/api/workflow/todo"),
                HttpMethod.GET, new HttpEntity<>(auth), String.class);
        String todoBody = todo.getBody();
        assertTrue(todoBody.contains("\"code\":0"), todoBody);
        assertTrue(todoBody.contains(instanceId), "待辦應包含新實例");
        assertTrue(todoBody.contains("主管審批"), todoBody);
        String taskId = extractFieldNear(todoBody, instanceId, "taskId");

        // 4. 批准
        HttpHeaders completeJson = new HttpHeaders();
        completeJson.setContentType(MediaType.APPLICATION_JSON);
        completeJson.put(HttpHeaders.AUTHORIZATION, auth.get(HttpHeaders.AUTHORIZATION));
        String completeBody = "{\"approved\":true,\"comment\":\"同意\"}";
        ResponseEntity<String> complete = restTemplate.exchange(url("/api/workflow/tasks/" + taskId + "/complete"),
                HttpMethod.POST, new HttpEntity<>(completeBody, completeJson), String.class);
        assertTrue(complete.getBody().contains("\"code\":0"), complete.getBody());

        // 5. 我的申請顯示已批准
        ResponseEntity<String> mine = restTemplate.exchange(url("/api/workflow/mine"),
                HttpMethod.GET, new HttpEntity<>(auth), String.class);
        String mineBody = mine.getBody();
        assertTrue(mineBody.contains(instanceId), mineBody);
        assertTrue(mineBody.contains("已批准"), mineBody);

        // 6. 表單管理接口可用
        ResponseEntity<String> forms = restTemplate.exchange(url("/api/workflow/forms/all"),
                HttpMethod.GET, new HttpEntity<>(auth), String.class);
        assertTrue(forms.getBody().contains("leave"), forms.getBody());
    }

    private String extractJsonStringField(String json, String field) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    /** 在實例 ID 附近取 taskId（同一段 JSON 內） */
    private String extractFieldNear(String json, String instanceId, String field) {
        int anchor = json.indexOf(instanceId);
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json.substring(anchor));
        return m.find() ? m.group(1) : null;
    }
}
