package com.epam.lstrsum.email;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.email.service.EmailParser;
import com.epam.lstrsum.email.service.ExchangeServiceHelper;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.QuestionService;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("email")
public class EmailParseAndSaveTest extends SetUpDataBaseCollections {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private QuestionService questionService;

    private ExchangeServiceHelper exchangeServiceHelper = mock(ExchangeServiceHelper.class);
    private EmailParser emailParser = new EmailParser(exchangeServiceHelper);

    @Before
    public void setUp() {
        when(exchangeServiceHelper.resolveGroup(anyString())).thenAnswer(invocation -> singletonList(invocation.getArguments()[0]));
    }

    @Test
    public void testThatReceivedEmailCreateNewQuestionAndSaveItToMongo() throws Exception {
        final User authorOfEmail = new User(new ObjectId().toString(), "Eugen", "Sandrov",
                "Eugen_Sandrov@epam.com", Arrays.asList(UserRoleType.ROLE_EXTENDED_USER, UserRoleType.ROLE_ADMIN), Instant.now(), true);
        final User receiverOfEmail = new User(new ObjectId().toString(), "Stan", "Chivs",
                "Stan_Chivs@epam.com", Collections.singletonList(UserRoleType.ROLE_SIMPLE_USER), Instant.now(), true);
        mongoTemplate.save(authorOfEmail);
        mongoTemplate.save(receiverOfEmail);
        final MimeMessage simpleEmail = javaMailSender.createMimeMessage();
        final MimeMessageHelper messageHelper = new MimeMessageHelper(simpleEmail);
        messageHelper.setSubject("Simple request title");
        messageHelper.setText("Simple request text");
        messageHelper.setFrom("Eugen_Sandrov@epam.com");
        messageHelper.setTo("Stan_Chivs@epam.com");
        final EmailParser.EmailForExperienceApplication parsedMessage = emailParser.getParsedMessage(simpleEmail);
        final QuestionPostDto requestPostDto = parsedMessage.getQuestionPostDto();
        questionService.addNewQuestion(requestPostDto, parsedMessage.getSender());
        final Question createdRequest = questionService.findQuestionByTitleAndAuthorEmail("Simple request title", authorOfEmail);
        assertThat(createdRequest.getTitle(), is("Simple request title"));
        assertThat(createdRequest.getText(), is("Simple request text"));
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(Question.class);
    }
}
