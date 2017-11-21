package com.epam.lstrsum.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.stream.IntStream;

@Service
@Profile("standalone")
@Primary
@RequiredArgsConstructor
public class MailServiceMockImpl implements MailService {
    private final CounterService counterService;

    @Override
    public void sendMessage(String subject, String text, String... to) {
        counterService.increment("mail.service.send.message");
    }

    @Override
    public void sendMessages(Collection<MimeMessage> mimeMessageCollection) {
        IntStream.range(0, mimeMessageCollection.size())
                .mapToObj(i -> "mail.service.send.message").forEach(counterService::increment);
    }
}
