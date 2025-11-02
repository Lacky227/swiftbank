package com.swiftbank.notificationservice.service;

import com.swiftbank.notificationservice.dto.MallingRequest;
import com.swiftbank.notificationservice.dto.ResetPayload;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class SendService {
    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender mailSender;
    private final RedisService redisService;

    @Value("${notification.from}")
    private String from;
    @Value("${notification.reset-password.subject}")
    private String subject;
    @Value("${notification.reset-password.link-en}")
    private String linkEN;
    @Value("${notification.reset-password.link-ua}")
    private String linkUA;

    public void forgotPassword(ResetPayload resetPayload) {
        String link;
        if (resetPayload.getLocale().equals("ua")) {
            link = linkUA;
        } else {
            link = linkEN;
        }
        MallingRequest mallingRequest = MallingRequest.builder()
                .email(resetPayload.getEmail())
                .subject(subject)
                .link(link + "?token=" + resetPayload.getResetToken())
                .locale(resetPayload.getLocale())
                .build();
        try {
            sendEmail(mallingRequest);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        redisService.saveToken(resetPayload.getEmail(), resetPayload.getResetToken());
    }

    private void sendEmail(MallingRequest request) throws MessagingException {
        Context context = new Context();

        context.setVariable("link", request.getLink());
        String html;
        if (request.getLocale().equals("ua")) {
            html = templateEngine.process("email-template-ua", context);
        } else{
            html = templateEngine.process("email-template-en", context);
        }
        String plainText = Jsoup.parse(html).text();

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setFrom(from);
        helper.setTo(request.getEmail());
        helper.setSubject(request.getSubject());
        helper.setText(plainText, html);

        mailSender.send(mimeMessage);
    }
}
