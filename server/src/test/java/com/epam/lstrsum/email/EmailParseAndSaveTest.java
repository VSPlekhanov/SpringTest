package com.epam.lstrsum.email;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.email.service.EmailParser;
import com.epam.lstrsum.email.service.ExchangeServiceHelper;
import com.epam.lstrsum.email.service.MailService;
import com.epam.lstrsum.email.template.NewErrorNotificationTemplate;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.QuestionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.TemplateEngine;

import javax.mail.internet.MimeMessage;
import java.util.EnumSet;

import static com.epam.lstrsum.enums.UserRoleType.*;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
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

    @Autowired
    private TemplateEngine templateEngine;

    @Mock
    private MailService mailService;

    private ExchangeServiceHelper exchangeServiceHelper = mock(ExchangeServiceHelper.class);
    private EmailParser emailParser = new EmailParser(exchangeServiceHelper);

    @Before
    public void setUp() {
        when(exchangeServiceHelper.resolveGroup(anyString())).thenAnswer(invocation -> singletonList(invocation.getArguments()[0]));
        emailParser.setMailService(mailService);
        NewErrorNotificationTemplate newErrorNotificationTemplate = new NewErrorNotificationTemplate();
        newErrorNotificationTemplate.setTemplateEngine(templateEngine);
        newErrorNotificationTemplate.setFromAddress("Auto_EPM-LSTR_Ask_Exp@epam.com");
        emailParser.setNewErrorNotificationTemplate(newErrorNotificationTemplate);
        emailParser.setMaxTextSize(16);
        emailParser.setMaxAttachmentsNumber(10);
        emailParser.setMaxAttachmentSize(16);
    }

    @Test
    public void testThatReceivedEmailCreateNewQuestionAndSaveItToMongo() throws Exception {
        final User authorOfEmail = User.builder()
                .email("Eugen_Sandrov@epam.com")
                .firstName(someString())
                .lastName(someString())
                .roles(EnumSet.of(ROLE_EXTENDED_USER, ROLE_ADMIN))
                .isActive(true)
                .build();
        final User receiverOfEmail = User.builder()
                .email("Stan_Chivs@epam.com")
                .firstName(someString())
                .lastName(someString())
                .roles(EnumSet.of(ROLE_SIMPLE_USER))
                .isActive(false)
                .build();

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
        questionService.addNewQuestionFromEmail(requestPostDto, parsedMessage.getSender());
        final Question createdRequest = questionService.findQuestionByTitleAndAuthorEmail("Simple request title", authorOfEmail);
        assertThat(createdRequest.getTitle(), is("Simple request title"));
        assertThat(createdRequest.getText(), is("Simple request text"));
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(Question.class);
    }

}
