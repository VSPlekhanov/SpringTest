package com.epam.lstrsum.mail.service;

import com.epam.lstrsum.model.Email;
import com.epam.lstrsum.persistence.EmailRepository;
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

import javax.mail.Address;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@ConfigurationProperties(prefix = "mail")
@Profile("email")
@Slf4j
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    private final String backupDateFormat = "yyyy-MM-dd_HH-mm-ss";

    @Setter
    private String fromAddress;

    @Setter
    private String backupDir = "";

    private final EmailRepository emailRepository;

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

        //InternetAddress address = (InternetAddress) message.getFrom()[0];
        String address = getAddressFrom(message.getFrom());

        debugLogMessage.append(
                "\nFrom: " + address +
                        " \nSubject: " + message.getSubject() +
                        " \nContent: \n" + content
        );

        log.debug(debugLogMessage.toString());
    }

    private String getAddressFrom(Address[] rawAddress) {
        InternetAddress internetAddress = (InternetAddress) rawAddress[0];
        return internetAddress.getAddress();
    }

    private void backupEmail(MimeMessage mimeMessage) throws IOException, MessagingException {
        // generate file name
        DateTimeFormatter date = DateTimeFormatter.ofPattern(backupDateFormat);
        LocalDateTime now = LocalDateTime.now();

        String baseFileName = date.format(now) + ".eml";
        String fullFileName = baseFileName + ".zip";

        // save email to db
        String addressFrom = getAddressFrom(mimeMessage.getFrom());

        Email email = new Email();
        email.setFileName(fullFileName);
        email.setFrom(addressFrom);
        email.setSubject(mimeMessage.getSubject());

        emailRepository.insert(email);

        // save email to file
        if (!backupDir.isEmpty()) {
            FileOutputStream fileOutput = new FileOutputStream(backupDir + fullFileName);

            ZipOutputStream zipOutput = new ZipOutputStream(fileOutput);

            ZipEntry zipEntry = new ZipEntry(baseFileName);
            zipOutput.putNextEntry(zipEntry);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            mimeMessage.writeTo(byteStream);

            zipOutput.write(byteStream.toByteArray());
            zipOutput.closeEntry();
            zipOutput.close();
        }
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
