package com.epam.lstrsum.email.service.impl;

import com.epam.lstrsum.email.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
@ConfigurationProperties(prefix = "email")
@Profile("email")
@Slf4j
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    @Setter
    private String fromAddress;

    public static String getAddressFrom(Address[] rawAddress) {
        InternetAddress internetAddress = (InternetAddress) rawAddress[0];
        return internetAddress.getAddress();
    }

    @Override
    public void sendMessage(String subject, String text, String... to) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mailMsg = new MimeMessageHelper(mimeMessage);
        mailMsg.setFrom(fromAddress);
        mailMsg.setTo(to);
        mailMsg.setSubject(subject);
        mailMsg.setText(text);
        sendMessage(mimeMessage);
    }

    @Override
    public void sendMessage(MimeMessage mimeMessage) throws MessagingException {
        mailSender.send(mimeMessage);
    }
}
