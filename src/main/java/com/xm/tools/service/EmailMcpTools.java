package com.xm.tools.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailMcpTools {

    private static final Logger log = LoggerFactory.getLogger(EmailMcpTools.class);

    private final JavaMailSender mailSender;

    /**
     * 发件人邮箱（从 SMTP_USERNAME 读取）
     */
    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailMcpTools(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Tool(description = "Send an email to a specified recipient. Supports plain text and HTML content.")
    public String sendEmail(
            @ToolParam(description = "Recipient email address") String to,
            @ToolParam(description = "Email subject line") String subject,
            @ToolParam(description = "Email body content") String body,
            @ToolParam(description = "CC recipients, comma-separated (optional)") String cc,
            @ToolParam(description = "BCC recipients, comma-separated (optional)") String bcc,
            @ToolParam(description = "Whether content is HTML") Boolean html) {

        try {
            boolean isHtml = html != null && html;

            if (isHtml) {
                sendHtmlEmail(to, subject, body, cc, bcc);
            } else {
                sendPlainTextEmail(to, subject, body, cc, bcc);
            }

            String result = String.format(
                    "✓ Email sent successfully to %s (subject: \"%s\")",
                    to,
                    subject
            );

            log.info(result);
            return result;

        } catch (Exception e) {

            String error = String.format(
                    "✗ Failed to send email to %s: %s",
                    to,
                    e.getMessage()
            );

            log.error(error, e);
            return error;
        }
    }

    /**
     * 发送纯文本邮件
     */
    private void sendPlainTextEmail(
            String to,
            String subject,
            String body,
            String cc,
            String bcc) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);

        // false = 纯文本
        helper.setText(body, false);

        if (cc != null && !cc.isBlank()) {
            helper.setCc(splitAddresses(cc));
        }

        if (bcc != null && !bcc.isBlank()) {
            helper.setBcc(splitAddresses(bcc));
        }

        mailSender.send(message);
    }

    /**
     * 发送HTML邮件
     */
    private void sendHtmlEmail(
            String to,
            String subject,
            String body,
            String cc,
            String bcc) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);

        // true = HTML
        helper.setText(body, true);

        if (cc != null && !cc.isBlank()) {
            helper.setCc(splitAddresses(cc));
        }

        if (bcc != null && !bcc.isBlank()) {
            helper.setBcc(splitAddresses(bcc));
        }

        mailSender.send(message);
    }

    /**
     * 解析多个邮箱地址
     */
    private String[] splitAddresses(String addresses) {
        return addresses.split("\\s*,\\s*");
    }
}