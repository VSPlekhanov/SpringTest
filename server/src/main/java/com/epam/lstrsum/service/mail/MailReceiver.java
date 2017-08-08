package com.epam.lstrsum.service.mail;

import com.epam.lstrsum.email.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.regex.Pattern;

import static com.epam.lstrsum.email.service.MailService.getAddressFrom;

@Service
@Profile("email")
@RequiredArgsConstructor
@Slf4j
public class MailReceiver {
    private static final Pattern EMAIL_MULTIPART_PATTERN = Pattern.compile("^multipart\\/.*");
    private static final Pattern EMAIL_TEXT_PATTERN = Pattern.compile("^text\\/.*");

    private final MailService mailService;

    @ServiceActivator(inputChannel = "receiveChannel", poller = @Poller(fixedRate = "200"))
    public void showMessages(MimeMessage message) throws Exception {
        log.debug("showMessages; Message received: {}", message);

        mailService.backupEmail(message);

        String contentType = message.getContentType();
        String content = "";
        String type = "";

        if (EMAIL_MULTIPART_PATTERN.asPredicate().test(contentType)) {
            MimeMultipart rawContent = (MimeMultipart) message.getContent();
            content = (String) rawContent.getBodyPart(0).getContent();
            type = "multi";
        } else if (EMAIL_TEXT_PATTERN.asPredicate().test(contentType)) {
            content = (String) message.getContent();
            type = "text";
        } else {
            log.warn("Unknown mime type!");
        }

        String address = getAddressFrom(message.getFrom());

        log.debug("From: {}\nSubject: {}\nContentType : \nContent: {}\nWith type: {}", address, message.getSubject(), contentType, content, type);
    }
}
