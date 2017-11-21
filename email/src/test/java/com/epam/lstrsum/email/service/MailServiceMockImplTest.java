package com.epam.lstrsum.email.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.actuate.metrics.CounterService;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class MailServiceMockImplTest {

    @Mock
    private CounterService counterService;

    @InjectMocks
    private MailServiceMockImpl mailServiceMock;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void sendMessage() {
        List<MimeMessage> fewMessagesInCollection = new LinkedList<>();
        fewMessagesInCollection.add(new MimeMessage((Session) null));
        fewMessagesInCollection.add(new MimeMessage((Session) null));

        List<MimeMessage> singleMessageInCollection = new LinkedList<>();
        singleMessageInCollection.add(new MimeMessage((Session) null));

        mailServiceMock.sendMessage("Test subject", "Test text");
        mailServiceMock.sendMessages(singleMessageInCollection);
        mailServiceMock.sendMessages(fewMessagesInCollection);

        verify(counterService, times(4)).increment(eq("mail.service.send.message"));
    }
}