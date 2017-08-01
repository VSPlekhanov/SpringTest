package com.epam.lstrsum.mail.service;

import com.epam.lstrsum.mail.exception.EmailValidationException;
import org.junit.Test;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class EmailParserTest {

    private EmailParser emailParser = new EmailParser();

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
        assertThat(senderEmail, is(equalTo("Eugen_Sandrov@epam.com")));
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
        assertThat(emailsFromHeader.contains("Donald_Zakary@epam.com"), is(true));
        assertThat(emailsFromHeader.contains("Stan_Chivs@epam.com"), is(true));
    }


}