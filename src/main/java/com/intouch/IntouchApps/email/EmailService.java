package com.intouch.IntouchApps.email;

import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
//import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {
    private final JavaMailSender mailSender;
    //    private  final SpringTemplateEngine templateEngine;
    private final SpringTemplateEngine templateEngine;

    // something to be added
    @Async
// This method need to be called from outside this class only as methods annotated with @Async need to be called by outside class only
    public void sendEmail(AppEmail appEmail) throws MessagingException, SendFailedException {
        String templateName;
        if (appEmail.getEmailTemplate() == null) {
            templateName = "confirm_email";
        } else {
            templateName = appEmail.getEmailTemplate().name().toLowerCase();
        }
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name()
        );
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", appEmail.getUsername());
        properties.put("confirmation_url", appEmail.getConfirmationUrl());
        properties.put("activation_code", appEmail.getActivationCode());
        properties.put("email_title", appEmail.getMessageTitle());
        properties.put("email_message", appEmail.getMessage());
        properties.put("confirmation_text", appEmail.getConfirmationText());

        Context context = new Context();
        context.setVariables(properties);

//        helper.setFrom("");
        helper.setTo(appEmail.getTo());
        helper.setSubject(appEmail.getSubject());

        String template = templateEngine.process(templateName, context);
        helper.setText(template, true);//attention here
        mailSender.send(mimeMessage);
    }
}
