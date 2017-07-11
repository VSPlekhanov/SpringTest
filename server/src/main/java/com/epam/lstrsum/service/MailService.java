package com.epam.lstrsum.service;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Profile("email")
@Slf4j
public class MailService {

    @ServiceActivator(inputChannel = "receiveChannel", poller = @Poller(fixedRate = "200"))
    public void showMessages(MimeMessage message) throws Exception {
        String contentType = message.getContentType();

        log.info(contentType);

        String content = "";
        if (matchesToRegexp(contentType, "^multipart\\/.*")) {
            MimeMultipart rawContent = (MimeMultipart) message.getContent();
            content = (String) rawContent.getBodyPart(0).getContent();

            log.info("mime is multi");
        } else if (matchesToRegexp(contentType, "^text\\/.*")) {
            content = (String) message.getContent();
            log.info("mime is text");
        } else {
            log.info("Unknown mime type!");
        }

        InternetAddress address = (InternetAddress) message.getFrom()[0];

        log.info("Email received ------------------------------------");

        log.info("\nFrom: %s \nSubject: %s \nContent: \n%s",
                address.getAddress(), message.getSubject(), content);

        log.info("\n\n------------------------------------");
    }

    private static boolean matchesToRegexp(String input, String regexp){
        Pattern p = Pattern.compile(regexp);
        return p.asPredicate().test(input);
    }
}
