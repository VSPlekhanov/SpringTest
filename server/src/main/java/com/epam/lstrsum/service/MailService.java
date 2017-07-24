package com.epam.lstrsum.service;

import com.epam.lstrsum.configuration.MailConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.regex.Pattern;

@Component
@Profile("email")
@Slf4j
public class MailService {

    @Autowired
    private JavaMailSenderImpl mailSender;

    @ServiceActivator(inputChannel = "receiveChannel", poller = @Poller(fixedRate = "200"))
    public void showMessages(MimeMessage message) throws Exception {
        log.debug("showMessages; Message received: {}", message);

        StringBuilder debugLogMessage = new StringBuilder();

        String contentType = message.getContentType();
        debugLogMessage.append(contentType);

        String content = "";
        if (matchesToRegexp(contentType, "^multipart\\/.*")) {
            MimeMultipart rawContent = (MimeMultipart) message.getContent();
            content = (String) rawContent.getBodyPart(0).getContent();

            debugLogMessage.append("mime is multi");
        } else if (matchesToRegexp(contentType, "^text\\/.*")) {
            content = (String) message.getContent();
            debugLogMessage.append("mime is text");
        } else {
            log.warn("Unknown mime type!");
        }

        InternetAddress address = (InternetAddress) message.getFrom()[0];

        debugLogMessage.append(
                "\nFrom: " + address.getAddress() +
                        " \nSubject: " + message.getSubject() +
                        " \nContent: \n" + content
        );

        log.debug(debugLogMessage.toString());
    }

    private static boolean matchesToRegexp(String input, String regexp) {
        Pattern p = Pattern.compile(regexp);
        return p.asPredicate().test(input);
    }


    private void sendMessage(String subject, String text, String... to) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mailMsg = new MimeMessageHelper(mimeMessage);
        mailMsg.setFrom("Auto_EPM-LSTR_Ask_Exp@epam.com");
        mailMsg.setTo(to);
        mailMsg.setSubject(subject);
        mailMsg.setText(text);
        mailSender.send(mimeMessage);
    }

    private void sendMessage(MimeMessage mimeMessage) throws MessagingException {
        mailSender.send(mimeMessage);
    }
}
