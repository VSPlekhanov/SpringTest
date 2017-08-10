package com.epam.lstrsum.email;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.email.service.MailService;
import com.epam.lstrsum.email.template.NewAnswerNotificationTemplate;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.QuestionService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Collections;

import static com.epam.lstrsum.testutils.InstantiateUtil.someLong;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("email")
public class AnswerNotificationAspectTest extends SetUpDataBaseCollections {

    @Autowired
    private NewAnswerNotificationTemplate answerTemplate;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @MockBean
    private MailService mailService;

    private String questionAuthorEmail;
    private String answerAuthorEmail;
    private QuestionPostDto questionPostDto;

    @Before
    public void setUpCommonAnswerAndQuestion() throws Exception {
        this.questionAuthorEmail = "John_Doe@epam.com";
        this.answerAuthorEmail = "Ernest_Hemingway@epam.com";
        this.questionPostDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                someString(), someLong(),
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", answerAuthorEmail));
    }

    @Test
    public void whenNewAnswerWasAddedMailWithExpectedSubjectShouldBeSend() throws Exception {
        MimeMessage expected = addNewQuestionAndAnswerToItInDBAndGetExpectedMimeMessage(
                questionPostDto, questionAuthorEmail, answerAuthorEmail);

        verify(mailService, times(2)).sendMessage(any());

        doAnswer(invocation -> {
            MimeMessage actual = (MimeMessage) invocation.getArguments()[0];
            assertThat(actual.getSubject(), equalTo(expected.getSubject()));
            return null;
        }).when(mailService).sendMessage(expected);
    }


    @Test
    public void whenNewAnswerWasAddedMailWithExpectedBodyAndBodyTypeShouldBeSent() throws Exception {
        questionPostDto.setTitle(someString());
        MimeMessage expected = addNewQuestionAndAnswerToItInDBAndGetExpectedMimeMessage(
                questionPostDto, questionAuthorEmail, answerAuthorEmail);

        verify(mailService, times(2)).sendMessage(any());

        doAnswer(invocation -> {
            MimeMessage actual = (MimeMessage) invocation.getArguments()[0];
            assertThat(actual.getContentType(), equalTo(expected.getContentType()));
            assertThat(actual.getContentType(), equalTo("text/html"));
            return null;
        }).when(mailService).sendMessage(expected);
    }

    @Test
    public void whenNewAnswerWasAddedMailShouldBeSendToAuthorOnlyIfQuestionHasNoSubscribersOrCC() throws Exception {
        QuestionPostDto questionWithNoCC = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                someString(), someLong(),
                Collections.emptyList());

        MimeMessage expected = addNewQuestionAndAnswerToItInDBAndGetExpectedMimeMessage(
                questionWithNoCC, questionAuthorEmail, answerAuthorEmail);

        verify(mailService, times(2)).sendMessage(any());

        doAnswer(invocation -> {
            MimeMessage actual = (MimeMessage) invocation.getArguments()[0];
            assertThat(actual.getRecipients(Message.RecipientType.TO), equalTo(expected.getRecipients(Message.RecipientType.TO)));
            assertThat(actual.getRecipients(Message.RecipientType.TO).length, equalTo(1));
            return null;
        }).when(mailService).sendMessage(expected);
    }

    @Test
    public void whenNewAnswerWasAddedMailShouldBeSendToAuthorAndCCOnlyIfQuestionHasNoSubscribers() throws Exception {
        questionPostDto.setTitle(someString());
        MimeMessage expected = addNewQuestionAndAnswerToItInDBAndGetExpectedMimeMessage(
                questionPostDto, questionAuthorEmail, answerAuthorEmail);

        verify(mailService, times(2)).sendMessage(any());

        doAnswer(invocation -> {
            MimeMessage actual = (MimeMessage) invocation.getArguments()[0];
            assertThat(actual.getRecipients(Message.RecipientType.TO), equalTo(expected.getRecipients(Message.RecipientType.TO)));
            assertThat(actual.getRecipients(Message.RecipientType.TO).length, equalTo(5));
            return null;
        }).when(mailService).sendMessage(expected);
    }

    @Test
    public void whenNewAnswerWasAddedMailShouldNotBeDuplicatedToOneUserIfUserIsDuplicatedInAuthorOrSubscriptionOrCCFields() throws Exception {
        QuestionPostDto questionWithDuplicates = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 11223344L,
                Collections.nCopies(4, questionAuthorEmail));

        MimeMessage expected = addNewQuestionAndAnswerToItInDBAndGetExpectedMimeMessage(
                questionWithDuplicates, questionAuthorEmail, answerAuthorEmail);

        verify(mailService, times(2)).sendMessage(any());

        doAnswer(invocation -> {
            MimeMessage actual = (MimeMessage) invocation.getArguments()[0];
            assertThat(actual.getRecipients(Message.RecipientType.TO).length, equalTo(1));
            return null;
        }).when(mailService).sendMessage(expected);
    }

    private MimeMessage addNewQuestionAndAnswerToItInDBAndGetExpectedMimeMessage(QuestionPostDto question, String questionAuthor,
                                                                                 String answerAuthor) throws Exception {
        Question savedQuestion = questionService.addNewQuestion(question, questionAuthor);

        AnswerPostDto answerPost = new AnswerPostDto(savedQuestion.getQuestionId(), "Text of Answer");

        AnswerAllFieldsDto savedAnswer = answerService.addNewAnswer(answerPost, answerAuthor);

        return answerTemplate.buildMailMessage(savedAnswer);
    }
}


