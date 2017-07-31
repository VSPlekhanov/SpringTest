package com.epam.lstrsum.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private MailService mailService;

    @Before
    public void setUp() {
        initMocks(this);

        mailService = new MailService(mailSender);
    }

    @Test
    public void showMessagesReceiveMultipartContent() throws Exception {
        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        doReturn("multipart/").when(mimeMessageMock).getContentType();

        doReturn(createMultipartContent()).when(mimeMessageMock).getContent();
        doReturn(new InternetAddress[]{new InternetAddress("fromAddress")}).when(mimeMessageMock).getFrom();

        mailService.setFromAddress("fromAddress");
        mailService.showMessages(mimeMessageMock);

        verify(mimeMessageMock, times(1)).getContentType();
        verify(mimeMessageMock, times(1)).getContent();
    }

    private static MimeMultipart createMultipartContent() throws MessagingException {
        MimeMultipart multipartContent = new MimeMultipart("multipartContent");
        multipartContent.addBodyPart(new MimeBodyPart(new ByteArrayInputStream(new byte[]{0,0,0})));
        return multipartContent;
    }

    @Test
    public void showMessagesReceiveTextContent() throws Exception {
        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        doReturn("text/").when(mimeMessageMock).getContentType();

        doReturn("content").when(mimeMessageMock).getContent();
        doReturn(new InternetAddress[]{new InternetAddress("fromAddress")}).when(mimeMessageMock).getFrom();

        mailService.setFromAddress("fromAddress");
        mailService.showMessages(mimeMessageMock);

        verify(mimeMessageMock, times(1)).getContentType();
        verify(mimeMessageMock, times(1)).getContent();
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

        mailService.setFromAddress("fromAddress");
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