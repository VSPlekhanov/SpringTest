package com.epam.lstrsum.service.mail;

import com.epam.lstrsum.aggregators.QuestionAggregator;
import com.epam.lstrsum.email.persistence.EmailRepository;
import com.epam.lstrsum.email.service.EmailParser;
import com.epam.lstrsum.email.service.ExchangeServiceHelper;
import com.epam.lstrsum.email.service.MailService;
import com.epam.lstrsum.service.AttachmentService;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.SubscriptionService;
import com.epam.lstrsum.service.UserService;
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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class MailReceiverTest {
    private MailReceiver mailReceiver;

    @Mock
    private JavaMailSender mailSender;

    private MailService mailService;

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private UserService userService;

    @Mock
    private QuestionService questionService;

    @Mock
    private AttachmentService attachmentService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private EmailParser emailParser;

    @Mock
    private ExchangeServiceHelper exchangeServiceHelper;

    @Mock
    private QuestionAggregator questionAggregator;

    private static MimeMultipart createMultipartContent() throws MessagingException {
        final MimeMultipart multipartContent = new MimeMultipart("multipartContent");
        multipartContent.addBodyPart(new MimeBodyPart(new ByteArrayInputStream(new byte[]{0, 0, 0})));
        return multipartContent;
    }

    @Before
    public void setUp() {
        initMocks(this);

        mailService = new MailService(mailSender, emailRepository);
        mailReceiver = new MailReceiver(
                mailService, userService,
                questionService, attachmentService, emailParser
        );
    }

    @Test
    public void showMessagesReceiveMultipartContent() throws Exception {
        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        doReturn("multipart/").when(mimeMessageMock).getContentType();

        doReturn(createMultipartContent()).when(mimeMessageMock).getContent();
        doReturn(new InternetAddress[]{new InternetAddress("fromAddress")}).when(mimeMessageMock).getFrom();

        mailReceiver.showMessages(mimeMessageMock);

        verify(mimeMessageMock, times(1)).getContentType();
        verify(mimeMessageMock, times(2)).getSubject();
    }

    @Test
    public void showMessagesReceiveTextContent() throws Exception {
        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        doReturn("text/").when(mimeMessageMock).getContentType();

        doReturn("content").when(mimeMessageMock).getContent();
        doReturn(new InternetAddress[]{new InternetAddress("fromAddress")}).when(mimeMessageMock).getFrom();

        mailReceiver.showMessages(mimeMessageMock);

        verify(mimeMessageMock, times(1)).getContentType();
        verify(mimeMessageMock, times(2)).getSubject();
    }
}