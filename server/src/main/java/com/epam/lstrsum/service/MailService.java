package com.epam.lstrsum.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "mail")
@Profile("email")
@Slf4j
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    @Setter
    private String fromAddress;

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


    public void sendMessage(String subject, String text, String... to) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mailMsg = new MimeMessageHelper(mimeMessage);
        mailMsg.setFrom(fromAddress);
        mailMsg.setTo(to);
        mailMsg.setSubject(subject);
        mailMsg.setText(text);
        sendMessage(mimeMessage);
    }

    public void sendMessage(MimeMessage mimeMessage) throws MessagingException {
        mailSender.send(mimeMessage);
    }
}
