package com.epam.lstrsum.mail.service;

import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.mail.exception.EmailValidationException;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.RequestService;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailParserServiceTest {


    @Autowired
    private EmailParserService parserService;
    @Autowired
    private RequestService requestService;

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test(expected = NullPointerException.class)
    public void TestThatParserThrowsExceptionWithNullArgs() throws Exception {
        final MimeMessage nullMessage = null;
        parserService.getParsedMessage(nullMessage);
    }

    @Test(expected = EmailValidationException.class)
    public void testThatParserThrowsExceptionWithEmptyTitleOfMessage() throws Exception {
        final MimeMessage withEmptyTitle = javaMailSender.createMimeMessage();
        final MimeMessageHelper messageHelper = new MimeMessageHelper(withEmptyTitle);
        messageHelper.setFrom("John_Doe@epam.com");
        messageHelper.setTo("John_Foe@epam.com");
        messageHelper.setSubject("       ");
        messageHelper.setText("Test text");
        parserService.getParsedMessage(withEmptyTitle);
    }

    @Test(expected = EmailValidationException.class)
    public void testThatParserThrowsExceptionWithEmptyBodyOfMessage() throws Exception {
        final MimeMessage withEmptyBody = javaMailSender.createMimeMessage();
        final MimeMessageHelper messageHelper = new MimeMessageHelper(withEmptyBody);
        messageHelper.setFrom("John_Doe@epam.com");
        messageHelper.setTo("John_Foe@epam.com");
        messageHelper.setSubject("Test title");
        messageHelper.setText("   ");
        parserService.getParsedMessage(withEmptyBody);
    }

    @Test
    public void testThatGetSenderMethodWorksRight() throws Exception {
        final MimeMessage normalMessage = javaMailSender.createMimeMessage();
        final MimeMessageHelper messageHelper = new MimeMessageHelper(normalMessage);
        messageHelper.setSubject("Title");
        messageHelper.setText("TextOf");
        messageHelper.setFrom("Eugen_Sandrov@epam.com");
        messageHelper.setTo("Roman_Karimov@epam.com");
        final String senderEmail = parserService.getParsedMessage(normalMessage).getSender();
        assertThat(senderEmail, is(equalTo("Eugen_Sandrov@epam.com")));
    }

    @Test
    public void testThatGetEmailsFromHeaderWorksRight() throws Exception {
        final MimeMessage normalMessage = javaMailSender.createMimeMessage();
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
        final List<String> emailsFromHeader = parserService.getParsedMessage(normalMessage).getReceivers();
        assertThat(emailsFromHeader.isEmpty(), is(false));
        assertThat(emailsFromHeader.size(), is(equalTo(9)));
        assertThat(emailsFromHeader.contains("Donald_Zakary@epam.com"), is(true));
        assertThat(emailsFromHeader.contains("Stan_Chivs@epam.com"), is(true));
    }

    @Test
    public void testThatReceivedEmailCreateNewRequestAndSaveItToFongo() throws Exception {
        final User authorOfEmail = new User(new ObjectId().toString(), "Eugen", "Sandrov",
                "Eugen_Sandrov@epam.com", new String[]{"ADMIN", "USER"}, Instant.now(), true);
        final User receiverOfEmail = new User(new ObjectId().toString(), "Stan", "Chivs",
                "Stan_Chivs@epam.com", new String[]{"USER"}, Instant.now(), true);
        mongoTemplate.save(authorOfEmail);
        mongoTemplate.save(receiverOfEmail);
        final MimeMessage simpleEmail = javaMailSender.createMimeMessage();
        final MimeMessageHelper messageHelper = new MimeMessageHelper(simpleEmail);
        messageHelper.setSubject("Simple request title");
        messageHelper.setText("Simple request text");
        messageHelper.setFrom("Eugen_Sandrov@epam.com");
        messageHelper.setTo("Stan_Chivs@epam.com");
        final EmailParserService.EmailForExperienceApplication parsedMessage = parserService.getParsedMessage(simpleEmail);
        if (!parsedMessage.isAnswer()){
            final RequestPostDto requestPostDto = parsedMessage.getRequestPostDto().orElse(null);
            requestService.addNewRequest(requestPostDto,parsedMessage.getSender());
        }
        final Request createdRequest = requestService.findRequestByTitleAndTextAndAuthorId("Simple request title", "Simple request text", authorOfEmail);
        assertThat(createdRequest.getTitle(), is("Simple request title"));
        assertThat(createdRequest.getText(), is("Simple request text"));
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(Request.class);
    }
}