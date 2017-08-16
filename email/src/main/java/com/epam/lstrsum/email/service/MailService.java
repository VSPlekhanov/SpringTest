package com.epam.lstrsum.email.service;

import com.epam.lstrsum.email.persistence.EmailRepository;
import com.epam.lstrsum.model.Email;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@ConfigurationProperties(prefix = "email")
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

    public static String getAddressFrom(Address[] rawAddress) {
        InternetAddress internetAddress = (InternetAddress) rawAddress[0];
        return internetAddress.getAddress();
    }

    public void backupEmail(MimeMessage mimeMessage) throws IOException, MessagingException {
        // generate file name
        DateTimeFormatter date = DateTimeFormatter.ofPattern(backupDateFormat);
        LocalDateTime now = LocalDateTime.now();

        String baseFileName = date.format(now) + ".eml";
        String fullFileName = baseFileName + ".zip";

        // save email to db
        String addressFrom = getAddressFrom(mimeMessage.getFrom());

        Email email = Email.builder()
                .fileName(fullFileName)
                .from(addressFrom)
                .subject(mimeMessage.getSubject())
                .build();
        emailRepository.insert(email);

        // save email to file
        if (!backupDir.isEmpty()) {
            ZipOutputStream zipOutput = new ZipOutputStream(
                    new FileOutputStream(backupDir + File.separator + fullFileName));

            ZipEntry zipEntry = new ZipEntry(baseFileName);
            zipOutput.putNextEntry(zipEntry);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            mimeMessage.writeTo(byteStream);

            zipOutput.write(byteStream.toByteArray());
            zipOutput.closeEntry();
            zipOutput.close();
        }
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
