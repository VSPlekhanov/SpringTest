package com.epam.lstrsum.service;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MailService {

    @ServiceActivator(inputChannel = "receiveChannel", poller = @Poller(fixedRate = "200"))
    public void showMessages(MimeMessage message) throws Exception {
        String contentType = message.getContentType();

        System.out.println(contentType);

        String content = "";
        if (matchesToRegexp(contentType, "^multipart\\/.*")) {
            MimeMultipart rawContent = (MimeMultipart) message.getContent();
            content = (String) rawContent.getBodyPart(0).getContent();

            System.out.println("mime is multi");
        } else if (matchesToRegexp(contentType, "^text\\/.*")) {
            content = (String) message.getContent();
            System.out.println("mime is text");
        } else {
            System.out.println("Unknown mime type!");
        }

        InternetAddress address = (InternetAddress) message.getFrom()[0];

        System.out.println("Email received ------------------------------------");

        System.out.printf("\nFrom: %s \nSubject: %s \nContent: \n%s",
                address.getAddress(), message.getSubject(), content);

        System.out.println("\n\n------------------------------------");
    }

    private static boolean matchesToRegexp(String input, String regexp){
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(input);
        return m.matches();
    }
}
