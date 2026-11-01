package com.novaboot.starter.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 郵件 Starter 配置
 *
 * <pre>
 * spring:
 *   mail:
 *     host: smtp.example.com
 *     port: 465
 *     username: no-reply@example.com
 *     password: xxx
 *     properties:
 *       mail.smtp.ssl.enable: true
 * nova:
 *   mail:
 *     from: no-reply@example.com   # 缺省取 spring.mail.username
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "nova.mail")
public class NovaMailProperties {

    /** 發件人（缺省取 spring.mail.username） */
    private String from;
}
