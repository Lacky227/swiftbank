package com.swiftbank.notificationservice.service;

import com.swiftbank.notificationservice.dto.MallingRequest;
import com.swiftbank.notificationservice.dto.ResetPayload;
import com.swiftbank.notificationservice.utils.HashUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class SendService {
    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender mailSender;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    @Value("${notification.from}")
    private String from;
    @Value("${notification.subject}")
    private String subject;
    @Value("${notification.link-en}")
    private String linkEN;
    @Value("${notification.link-ua}")
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
        redisService.saveToken(resetPayload.getEmail(), HashUtils.sha256(resetPayload.getResetToken()));
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
