package com.novaboot.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.Result;
import com.novaboot.starter.ai.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * M3 AI Starter 演示接口（nova.ai.enabled=true 時註冊）
 */
@RestController
@RequestMapping("/api/ai")
@ConditionalOnProperty(name = "nova.ai.enabled", havingValue = "true")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    public record ChatBody(String prompt) {
    }

    @PostMapping("/chat")
    @SaCheckPermission("ai:chat")
    public Result<String> chat(@RequestBody ChatBody body) {
        if (body == null || body.prompt() == null || body.prompt().isBlank()) {
            return Result.fail("prompt 不能為空");
        }
        return Result.ok(aiChatService.chat(body.prompt()));
    }
}
