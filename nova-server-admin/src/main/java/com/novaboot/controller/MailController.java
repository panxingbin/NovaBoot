package com.novaboot.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.Result;
import com.novaboot.starter.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * M3 郵件 Starter 演示接口（spring.mail.host 已配置時註冊）
 */
@RestController
@RequestMapping("/api/mail")
@ConditionalOnProperty(name = "spring.mail.host")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    public record SendBody(String to, String subject, String content, boolean html) {
    }

    @PostMapping("/send")
    @SaCheckPermission("mail:send")
    public Result<Void> send(@RequestBody SendBody body) {
        if (body == null || body.to() == null || body.to().isBlank()) {
            return Result.fail("收件人不能為空");
        }
        if (body.html()) {
            mailService.sendHtml(body.to(), body.subject(), body.content());
        } else {
            mailService.sendSimple(body.to(), body.subject(), body.content());
        }
        return Result.ok();
    }
}
