package com.epam.lstrsum.email.service;

import com.epam.lstrsum.email.service.impl.MailServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private MailService mailService;

    @Before
    public void setUp() {
        initMocks(this);
        mailService = new MailServiceImpl(mailSender);
    }

    @Test
    public void sendMessageWithoutFromAddress() throws Exception {
        assertThatThrownBy(() -> mailService.sendMessage("subject", "text", "to1", "to2"))
                .hasMessageMatching(".*address must not be null.*");
    }

    @Test
    public void sendMessageCorrect() throws Exception {
        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessageMock);

        ((MailServiceImpl) mailService).setFromAddress("fromAddress");
        String text = "text";
        String subject = "subject";
        mailService.sendMessage(subject, text, "to1", "to2");

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(eq(mimeMessageMock));
        verify(mimeMessageMock, times(1)).setFrom(any(InternetAddress.class));
        verify(mimeMessageMock, times(1)).setRecipients(eq(Message.RecipientType.TO), any(InternetAddress[].class));
        verify(mimeMessageMock, times(1)).setSubject(eq(subject));
        verify(mimeMessageMock, times(1)).setText(eq(text));
    }

}