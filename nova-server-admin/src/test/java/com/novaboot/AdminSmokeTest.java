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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 核心鏈路冒煙測試（local profile：H2 內存庫 + 內存會話 + 驗證碼關閉）
 * 覆蓋：登入 → 會話 → 權限數據源 → 未登入攔截 → 檔案下載鑑權 → 登出失效
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class AdminSmokeTest {

    private static final Pattern TOKEN_FIELD = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void loginProfileAndAccessControlChain() {
        // 1. 未登入訪問受保護接口 → 業務碼 401
        ResponseEntity<String> noAuth = restTemplate.getForEntity(url("/api/system/users"), String.class);
        assertTrue(bodyOf(noAuth).contains("\"code\":401"), bodyOf(noAuth));

        // 2. 未登入下載檔案 → 業務碼 401（修復前該接口在登入白名單中，可匿名遍歷下載）
        ResponseEntity<String> downloadNoAuth =
                restTemplate.getForEntity(url("/api/system/files/download/1"), String.class);
        assertTrue(bodyOf(downloadNoAuth).contains("\"code\":401"), bodyOf(downloadNoAuth));

        // 3. 登入（local 關閉驗證碼；管理員密碼由 InitAdminPasswordRunner 同步為 admin123）
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> login = restTemplate.postForEntity(url("/api/auth/login"),
                new HttpEntity<>("{\"username\":\"admin\",\"password\":\"admin123\"}", loginHeaders), String.class);
        String loginBody = bodyOf(login);
        assertTrue(loginBody.contains("\"code\":0"), loginBody);
        String token = extractToken(loginBody);
        assertFalse(token.isBlank(), "登入響應應包含 token");

        // 4. 攜帶 Token 取個人信息 → 角色含超級管理員，權限為通配
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(token);
        ResponseEntity<String> profile = restTemplate.exchange(url("/api/auth/profile"),
                HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);
        String profileBody = bodyOf(profile);
        assertTrue(profileBody.contains("\"code\":0"), profileBody);
        assertTrue(profileBody.contains("admin"), profileBody);

        // 5. 攜帶 Token 查詢用戶分頁 → 成功（RBAC 數據源 + 分頁插件可用）
        ResponseEntity<String> users = restTemplate.exchange(
                url("/api/system/users?pageNum=1&pageSize=10"),
                HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);
        assertTrue(bodyOf(users).contains("\"code\":0"), bodyOf(users));

        // 6. 攜帶 Token 取路由選單樹 → 管理員可見全部（含系統管理目錄）
        ResponseEntity<String> menus = restTemplate.exchange(url("/api/system/menus/tree"),
                HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);
        String menusBody = bodyOf(menus);
        assertTrue(menusBody.contains("\"code\":0"), menusBody);
        assertTrue(menusBody.contains("系統管理"), menusBody);

        // 7. 登出後原 Token 再訪問 → 401
        restTemplate.exchange(url("/api/auth/logout"), HttpMethod.POST,
                new HttpEntity<>(authHeaders), String.class);
        ResponseEntity<String> afterLogout = restTemplate.exchange(url("/api/auth/profile"),
                HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);
        assertTrue(bodyOf(afterLogout).contains("\"code\":401"), bodyOf(afterLogout));
    }

    private String bodyOf(ResponseEntity<String> response) {
        return response.getBody() == null ? "" : response.getBody();
    }

    private String extractToken(String loginBody) {
        Matcher matcher = TOKEN_FIELD.matcher(loginBody);
        return matcher.find() ? matcher.group(1) : "";
    }
}
