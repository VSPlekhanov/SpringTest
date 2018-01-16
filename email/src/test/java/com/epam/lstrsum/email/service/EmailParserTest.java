package com.epam.lstrsum.email.service;

import com.epam.lstrsum.email.exception.EmailValidationException;
import com.epam.lstrsum.email.template.NewErrorNotificationTemplate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.List;

import static com.epam.lstrsum.email.service.EmailParserUtil.getSender;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmailParserTest {

    @Mock
    private MailService mailService;

    private ExchangeServiceHelper exchangeServiceHelper = mock(ExchangeServiceHelper.class);
    private EmailParser emailParser = new EmailParser(exchangeServiceHelper);

    @Before
    public void setUp() {
        when(exchangeServiceHelper.resolveGroup(anyString())).thenAnswer(invocation -> singletonList(invocation.getArguments()[0]));
        TemplateEngine templateEngine = new TemplateEngine();
        NewErrorNotificationTemplate newErrorNotificationTemplate = new NewErrorNotificationTemplate();
        newErrorNotificationTemplate.setTemplateEngine(templateEngine);
        emailParser.setMailService(mailService);
        emailParser.setNewErrorNotificationTemplate(newErrorNotificationTemplate);
        emailParser.setMaxTextSize(16);
        emailParser.setMaxAttachmentsNumber(10);
        emailParser.setMaxAttachmentSize(16);
    }

    @Test(expected = NullPointerException.class)
    public void TestThatParserThrowsExceptionWithNullArgs() throws Exception {
        final MimeMessage nullMessage = null;
        emailParser.getParsedMessage(nullMessage);
    }

    @Test(expected = EmailValidationException.class)
    public void testThatParserThrowsExceptionWithEmptyTitleOfMessage() throws Exception {
        final MimeMessage withEmptyTitle = new MimeMessage((Session) null);
        final MimeMessageHelper messageHelper = new MimeMessageHelper(withEmptyTitle);
        messageHelper.setFrom("John_Doe@epam.com");
        messageHelper.setTo("John_Foe@epam.com");
        messageHelper.setSubject("       ");
        messageHelper.setText("Test text");
        emailParser.getParsedMessage(withEmptyTitle);
    }

    @Test(expected = EmailValidationException.class)
    public void testThatParserThrowsExceptionWithEmptyBodyOfMessage() throws Exception {
        final MimeMessage withEmptyBody = new MimeMessage((Session) null);
        final MimeMessageHelper messageHelper = new MimeMessageHelper(withEmptyBody);
        messageHelper.setFrom("John_Doe@epam.com");
        messageHelper.setTo("John_Foe@epam.com");
        messageHelper.setSubject("Test title");
        messageHelper.setText("   ");
        emailParser.getParsedMessage(withEmptyBody);
    }

    @Test
    public void testThatGetSenderMethodWorksRight() throws Exception {
        final MimeMessage normalMessage = new MimeMessage((Session) null);
        final MimeMessageHelper messageHelper = new MimeMessageHelper(normalMessage);
        messageHelper.setSubject("Title");
        messageHelper.setText("TextOf");
        messageHelper.setFrom("Eugen_Sandrov@epam.com");
        messageHelper.setTo("Roman_Karimov@epam.com");
        final String senderEmail = emailParser.getParsedMessage(normalMessage).getSender();
        assertThat(senderEmail, is(equalTo("eugen_sandrov@epam.com")));
    }

    @Test
    public void testThatGetEmailsFromHeaderWorksRight() throws Exception {
        final MimeMessage normalMessage = new MimeMessage((Session) null);
        final MimeMessageHelper messageHelper = new MimeMessageHelper(normalMessage);
        messageHelper.setSubject("Title");
        messageHelper.setText("TextOf");
        messageHelper.setFrom("Eugen_Sandrov@epam.com");
        messageHelper.setTo(new String[]{"Roman_Karimov@epam.com",
                "Hanna_Davydovich@epam.com",
                "Stan_Chivs@epam.com",
                "Kim_Chen@epam.com"});
        messageHelper.setCc(new String[]{"Michael_Fig@epam.com",
                "Rayan_Morren@epam.com",
                "Donald_Zakary@epam.com",
                "Gor_Sil@epam.com",
                "Kate_Sunfield@epam.com"});
        final List<String> emailsFromHeader = emailParser.getParsedMessage(normalMessage).getReceivers();
        assertThat(emailsFromHeader.isEmpty(), is(false));
        assertThat(emailsFromHeader.size(), is(equalTo(9)));
        assertThat(getSender(normalMessage), is("eugen_sandrov@epam.com"));
        assertThat(emailsFromHeader.contains("donald_zakary@epam.com"), is(true));
        assertThat(emailsFromHeader.contains("stan_chivs@epam.com"), is(true));
    }

    @Test(expected = EmailValidationException.class)
    public void testThatParserThrowsExceptionWhenTextExceededSizeLimits() throws Exception {
        final MimeMessage messageWithBigText = new MimeMessage((Session) null);
        final MimeMessageHelper messageHelper = new MimeMessageHelper(messageWithBigText);
        messageHelper.setFrom("John_Doe@epam.com");
        messageHelper.setTo("John_Foe@epam.com");
        messageHelper.setSubject("       ");
        messageHelper.setText(createStringWithSpecificSize(Integer.MAX_VALUE / 100));
        emailParser.getParsedMessage(messageWithBigText);
    }

    private String createStringWithSpecificSize(int stringSize) {
        StringBuilder sb = new StringBuilder(stringSize);
        for (int i=0; i < stringSize; i++) {
            sb.append('a');
        }
        return sb.toString();
    }
}