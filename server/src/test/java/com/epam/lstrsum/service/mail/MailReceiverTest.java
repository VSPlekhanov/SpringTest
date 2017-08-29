package com.epam.lstrsum.service.mail;

import com.epam.lstrsum.aggregators.QuestionAggregator;
import com.epam.lstrsum.email.persistence.EmailRepository;
import com.epam.lstrsum.email.service.BackupHelper;
import com.epam.lstrsum.email.service.EmailParser;
import com.epam.lstrsum.email.service.ExchangeServiceHelper;
import com.epam.lstrsum.service.AttachmentService;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.SubscriptionService;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.service.impl.MailReceiverImpl;
import com.epam.lstrsum.testutils.InstantiateUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class MailReceiverTest {
    @Mock
    private JavaMailSender mailSender;

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

    @Mock
    private BackupHelper backupHelper;

    @Mock
    private TelescopeService telescopeService;

    @InjectMocks
    private MailReceiverImpl mailReceiver;

    private static MimeMultipart createMultipartContent() throws MessagingException {
        final MimeMultipart multipartContent = new MimeMultipart("multipartContent");
        multipartContent.addBodyPart(new MimeBodyPart(new ByteArrayInputStream(new byte[]{0, 0, 0})));
        return multipartContent;
    }

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void showMessagesReceiveMultipartContent() throws Exception {
        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        doReturn("multipart/").when(mimeMessageMock).getContentType();
        doReturn(singletonList(InstantiateUtil.someTelescopeEmployeeEntityDto())).when(telescopeService)
                .getUsersInfoByEmails(anySetOf(String.class));

        doReturn(createMultipartContent()).when(mimeMessageMock).getContent();
        doReturn(new InternetAddress[]{new InternetAddress("fromAddress")}).when(mimeMessageMock).getFrom();

        mailReceiver.receiveMessageAndHandleIt(mimeMessageMock);

        verify(mimeMessageMock, times(1)).getContentType();
        verify(mimeMessageMock, times(1)).getSubject();
    }

    @Test
    public void showMessagesReceiveTextContent() throws Exception {
        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        doReturn("text/").when(mimeMessageMock).getContentType();
        doReturn(singletonList(InstantiateUtil.someTelescopeEmployeeEntityDto())).when(telescopeService)
                .getUsersInfoByEmails(anySetOf(String.class));

        doReturn("content").when(mimeMessageMock).getContent();
        doReturn(new InternetAddress[]{new InternetAddress("fromAddress")}).when(mimeMessageMock).getFrom();

        mailReceiver.receiveMessageAndHandleIt(mimeMessageMock);

        verify(mimeMessageMock, times(1)).getContentType();
        verify(mimeMessageMock, times(1)).getSubject();
    }

    @Test
    public void doNotHandleMessageFromNotEpamAccount() throws Exception {
        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        doReturn("text/").when(mimeMessageMock).getContentType();
        doReturn(Collections.emptyList()).when(telescopeService).getUsersInfoByEmails(anySetOf(String.class));

        doReturn("content").when(mimeMessageMock).getContent();
        doReturn(new InternetAddress[]{new InternetAddress("fromAddress")}).when(mimeMessageMock).getFrom();

        mailReceiver.receiveMessageAndHandleIt(mimeMessageMock);

        verify(mimeMessageMock, times(1)).getFrom();
        verify(mimeMessageMock, times(0)).getContentType();
        verify(mimeMessageMock, times(0)).getSubject();
    }
}