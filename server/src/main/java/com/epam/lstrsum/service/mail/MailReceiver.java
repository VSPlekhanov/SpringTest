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

    private final MailService mailService;

    @ServiceActivator(inputChannel = "receiveChannel", poller = @Poller(fixedRate = "200"))
    public void showMessages(MimeMessage message) throws Exception {
        log.debug("showMessages; Message received: {}", message);

        mailService.backupEmail(message);

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

        //InternetAddress address = (InternetAddress) message.getFrom()[0];
        String address = getAddressFrom(message.getFrom());

        debugLogMessage.append(
                "\nFrom: " + address +
                        " \nSubject: " + message.getSubject() +
                        " \nContent: \n" + content
        );

        log.debug(debugLogMessage.toString());
    }

    private static boolean matchesToRegexp(String input, String regexp) {
        Pattern p = Pattern.compile(regexp);
        return p.asPredicate().test(input);
    }

}
