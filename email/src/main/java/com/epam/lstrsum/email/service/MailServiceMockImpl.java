package com.epam.lstrsum.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@Profile("standalone")
@Primary
@RequiredArgsConstructor
public class MailServiceMockImpl implements MailService {
    private final CounterService counterService;

    @Override
    public void sendMessage(String subject, String text, String... to) throws MessagingException {
        counterService.increment("mail.service.send.message");
    }

    @Override
    public void sendMessage(MimeMessage mimeMessage) throws MessagingException {
        counterService.increment("mail.service.send.message");
    }
}
