package com.epam.lstrsum.service.mail;

import com.epam.lstrsum.email.persistence.EmailRepository;
import com.epam.lstrsum.email.service.MailService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;


public class MailReceiverTest {
    private MailReceiver mailReceiver;

    @Mock
    private JavaMailSender mailSender;

    private MailService mailService;

    @Mock
    private EmailRepository emailRepository;

    @Before
    public void setUp() {
        initMocks(this);

        mailService = new MailService(mailSender, emailRepository);
        mailReceiver = new MailReceiver(mailService);
    }

    @Test
    public void showMessagesReceiveMultipartContent() throws Exception {
        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        doReturn("multipart/").when(mimeMessageMock).getContentType();

        doReturn(createMultipartContent()).when(mimeMessageMock).getContent();
        doReturn(new InternetAddress[]{new InternetAddress("fromAddress")}).when(mimeMessageMock).getFrom();

        mailReceiver.showMessages(mimeMessageMock);

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

        mailReceiver.showMessages(mimeMessageMock);

        verify(mimeMessageMock, times(1)).getContentType();
        verify(mimeMessageMock, times(1)).getContent();
    }
}