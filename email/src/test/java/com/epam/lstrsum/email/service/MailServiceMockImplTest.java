package com.epam.lstrsum.email.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.actuate.metrics.CounterService;

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
    public void sendMessage() throws Exception {
        mailServiceMock.sendMessage(null);
        mailServiceMock.sendMessage(null, null);

        verify(counterService, times(2)).increment(eq("mail.service.send.message"));
    }
}