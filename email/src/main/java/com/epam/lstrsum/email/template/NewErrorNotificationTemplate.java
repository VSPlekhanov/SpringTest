package com.epam.lstrsum.email.template;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Component
@Profile("email")
@RequiredArgsConstructor
@Slf4j
public class NewErrorNotificationTemplate implements MailTemplate<List> {

    @Autowired
    @Setter
    private TemplateEngine templateEngine;

    @Setter
    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public Collection<MimeMessage> buildMailMessages(List errorList, boolean fromPortal) throws MessagingException {

        List<MimeMessage> mimeMessageCollection = new LinkedList<>();
        List<String> errors = new LinkedList<>();
        for (Object entry: errorList) {
            errors.add((String) entry);
        }

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        mimeMessage.setFrom(new InternetAddress(fromAddress));

        Context context = new Context();
        context.setVariable("errors", errors);

        mimeMessage.setFrom(new InternetAddress(fromAddress));
        mimeMessage.setSubject("Some errors occurred during email parsing...");
        mimeMessage.setText(templateEngine.process("newErrorNotification", context),
                "utf-8",
                "html");

        mimeMessage.setRecipients(Message.RecipientType.TO, errors.get(0));
        return mimeMessageCollection;
    }
}
