package com.epam.lstrsum.email;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.email.service.EmailParser;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.QuestionService;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.internet.MimeMessage;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@ActiveProfiles("email")
public class EmailParseAndSaveTest extends SetUpDataBaseCollections {

    private EmailParser emailParser = new EmailParser();

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private QuestionService questionService;

    @Test
    public void testThatReceivedEmailCreateNewQuestionAndSaveItToMongo() throws Exception {
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
        final EmailParser.EmailForExperienceApplication parsedMessage = emailParser.getParsedMessage(simpleEmail);
        if (!parsedMessage.isAnswer()){
            final QuestionPostDto requestPostDto = parsedMessage.getQuestionPostDto().orElse(null);
            questionService.addNewQuestion(requestPostDto,parsedMessage.getSender());
        }
        final Question createdRequest = questionService.findQuestionByTitleAndTextAndAuthorId("Simple request title", "Simple request text", authorOfEmail);
        assertThat(createdRequest.getTitle(), is("Simple request title"));
        assertThat(createdRequest.getText(), is("Simple request text"));
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(Question.class);
    }
}
