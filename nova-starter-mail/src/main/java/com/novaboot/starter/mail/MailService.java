package com.novaboot.starter.mail;

import com.novaboot.common.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 郵件發送服務。未配置 SMTP（spring.mail.host）時拋出業務異常，不影響應用啟動。
 */
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final String from;

    public MailService(JavaMailSender mailSender, NovaMailProperties properties) {
        this.mailSender = mailSender;
        this.from = properties != null && StringUtils.hasText(properties.getFrom())
                ? properties.getFrom()
                : null;
    }

    /** 是否可用（已配置 SMTP） */
    public boolean isConfigured() {
        return mailSender != null;
    }

    /**
     * 發送純文本郵件
     */
    public void sendSimple(String to, String subject, String content) {
        if (!isConfigured()) {
            throw new BusinessException("郵件未配置：請設置 spring.mail.host 等參數");
        }
        try {
            mailSender.send(prepareSimple(to, subject, content));
            log.info("郵件已發送: to={}, subject={}", to, subject);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("郵件發送失敗: to={}, subject={}", to, subject, e);
            throw new BusinessException("郵件發送失敗：" + e.getMessage());
        }
    }

    /**
     * 發送 HTML 郵件
     */
    public void sendHtml(String to, String subject, String html) {
        if (!isConfigured()) {
            throw new BusinessException("郵件未配置：請設置 spring.mail.host 等參數");
        }
        try {
            mailSender.send(prepareHtml(to, subject, html));
            log.info("HTML 郵件已發送: to={}, subject={}", to, subject);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("HTML 郵件發送失敗: to={}, subject={}", to, subject, e);
            throw new BusinessException("郵件發送失敗：" + e.getMessage());
        }
    }

    SimpleMailMessage prepareSimple(String to, String subject, String content) {
        requireFrom();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        return message;
    }

    MimeMessage prepareHtml(String to, String subject, String html) throws MessagingException {
        requireFrom();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        return message;
    }

    private void requireFrom() {
        if (!StringUtils.hasText(from)) {
            throw new BusinessException("缺少發件人：請配置 nova.mail.from 或 spring.mail.username");
        }
    }
}
