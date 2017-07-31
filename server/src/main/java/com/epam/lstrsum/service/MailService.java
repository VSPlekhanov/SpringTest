package com.epam.lstrsum.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "mail")
@Profile("email")
@Slf4j
public class MailService {

    @Autowired
    private MailSender mailSender;

    @Setter
    private String fromAddress;

    @Setter
    private String backupDir;

    @ServiceActivator(inputChannel = "receiveChannel", poller = @Poller(fixedRate = "200"))
    public void showMessages(MimeMessage message) throws Exception {
        log.debug("showMessages; Message received: {}", message);

        backupEmail(message);

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

    private void backupEmail(MimeMessage mimeMessage) throws IOException, MessagingException {
        if (backupDir.isEmpty()) {
            return;
        }

        DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();

        FileOutputStream output = new FileOutputStream(backupDir + date.format(now) + ".eml");
        mimeMessage.writeTo(output);
    }

    private static boolean matchesToRegexp(String input, String regexp) {
        Pattern p = Pattern.compile(regexp);
        return p.asPredicate().test(input);
    }


    public void sendMessage(String subject, String text, String... to) throws MessagingException {
        MimeMessage mimeMessage = ((JavaMailSenderImpl) mailSender).createMimeMessage();
        MimeMessageHelper mailMsg = new MimeMessageHelper(mimeMessage);
        mailMsg.setFrom(fromAddress);
        mailMsg.setTo(to);
        mailMsg.setSubject(subject);
        mailMsg.setText(text);
        ((JavaMailSenderImpl)mailSender).send(mimeMessage);
    }

    public void sendMessage(MimeMessage mimeMessage) throws MessagingException {
        ((JavaMailSenderImpl) mailSender).send(mimeMessage);
    }
}
