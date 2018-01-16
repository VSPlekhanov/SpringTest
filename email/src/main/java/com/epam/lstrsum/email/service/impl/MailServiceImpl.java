package com.epam.lstrsum.email.service.impl;

import com.epam.lstrsum.email.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import static java.util.Objects.isNull;

@Service
@Profile("email")
@Slf4j
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    @Setter
    @Value("${spring.mail.username}")
    private String fromAddress;

    public static String getAddressFrom(Address[] rawAddress) {
        InternetAddress internetAddress = (InternetAddress) rawAddress[0];
        return internetAddress.getAddress();
    }

    @Override
    public void sendMessage(String subject, String text, String... to) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mailMsg = new MimeMessageHelper(mimeMessage);
        mailMsg.setTo(to);
        mailMsg.setSubject(subject);
        mailMsg.setText(text);
        mailMsg.setFrom(new InternetAddress(fromAddress));
        Collection<MimeMessage> mimeMessageCollection = new LinkedList<>();
        mimeMessageCollection.add(mailMsg.getMimeMessage());
        sendMessages(mimeMessageCollection);
    }

    @Async
    @Override
    public void sendMessages(Collection<MimeMessage> mimeMessageCollection) throws MessagingException {
        MimeMessage mimeMessage;
        for (MimeMessage nextMimeMessage : mimeMessageCollection) {
            if (isNull((mimeMessage = nextMimeMessage).getFrom())) {
                log.warn("From address is null, i set it correct, but you should do it, please correct your code");
                mimeMessage.setFrom(new InternetAddress(fromAddress));
            }
            mailSender.send(mimeMessage);
        }
    }
}