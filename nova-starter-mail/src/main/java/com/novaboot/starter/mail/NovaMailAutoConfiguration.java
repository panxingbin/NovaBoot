package com.novaboot.starter.mail;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 郵件 Starter 自動配置：應用未配置 SMTP 時 JavaMailSender 缺席，服務以「未配置」狀態存在
 */
@AutoConfiguration
@ConditionalOnClass(JavaMailSender.class)
@EnableConfigurationProperties(NovaMailProperties.class)
public class NovaMailAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MailService mailService(ObjectProvider<JavaMailSender> mailSender, NovaMailProperties properties) {
        return new MailService(mailSender.getIfAvailable(), properties);
    }
}
