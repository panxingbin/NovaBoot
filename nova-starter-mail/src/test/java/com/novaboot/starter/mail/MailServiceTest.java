package com.novaboot.starter.mail;

import com.novaboot.common.exception.BusinessException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 郵件服務測試：只驗證消息組裝與未配置行為，不發起 SMTP 連接
 */
class MailServiceTest {

    private NovaMailProperties properties(String from) {
        NovaMailProperties properties = new NovaMailProperties();
        properties.setFrom(from);
        return properties;
    }

    @Test
    void unconfiguredSenderThrowsOnSend() {
        MailService service = new MailService(null, properties(null));
        assertFalse(service.isConfigured());
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.sendSimple("a@b.c", "主題", "內容"));
        assertTrue(e.getMessage().contains("spring.mail.host"));
    }

    @Test
    void missingFromThrowsWithGuidance() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        MailService service = new MailService(sender, properties(""));
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.prepareSimple("a@b.c", "主題", "內容"));
        assertTrue(e.getMessage().contains("nova.mail.from"));
    }

    @Test
    void prepareSimpleAssemblesMessage() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        MailService service = new MailService(sender, properties("no-reply@novaboot.dev"));

        var message = service.prepareSimple("user@example.com", "歡迎", "你好");

        assertEquals("no-reply@novaboot.dev", message.getFrom());
        assertArrayContains(message.getTo(), "user@example.com");
        assertEquals("歡迎", message.getSubject());
        assertEquals("你好", message.getText());
    }

    @Test
    void prepareHtmlAssemblesMimeMessage() throws Exception {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        MailService service = new MailService(sender, properties("no-reply@novaboot.dev"));

        MimeMessage message = service.prepareHtml("user@example.com", "驗證碼", "<b>123456</b>");

        assertEquals("no-reply@novaboot.dev", message.getFrom()[0].toString());
        assertEquals("驗證碼", message.getSubject());
        assertTrue(message.getContent().toString().contains("123456"));
    }

    private void assertArrayContains(String[] array, String expected) {
        for (String item : array) {
            if (item.equals(expected)) {
                return;
            }
        }
        throw new AssertionError("array does not contain " + expected);
    }
}
