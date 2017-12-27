package com.epam.lstrsum.email;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.email.service.MailService;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.QuestionService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

import static com.epam.lstrsum.testutils.InstantiateUtil.someLong;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ActiveProfiles("email")
public class AnswerNotificationAspectTest extends SetUpDataBaseCollections {

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
    public void setUpCommonAnswerAndQuestion() {
        this.questionAuthorEmail = "John_Doe@epam.com";
        this.answerAuthorEmail = "Ernest_Hemingway@epam.com";
        this.questionPostDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                someString(), someLong(),
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", answerAuthorEmail), Collections.emptyList());
    }

    @Test
    public void whenNewAnswerWasAddedMailWithExpectedSubjectAndBodyTypeShouldBeSend() throws Exception {

        doAnswer(invocation -> {
            LinkedList<MimeMessage> actualMimeMessageList = new LinkedList<>(
                    (Collection<MimeMessage>) invocation.getArguments()[0]);
            MimeMessage actualCreatorMimeMessage = actualMimeMessageList.get(0);
            MimeMessage actualSubscriberMimeMessage = actualMimeMessageList.get(1);

            assertThat(actualMimeMessageList.size(), equalTo(2));

            assertThat(actualCreatorMimeMessage.getContentType(), equalTo("text/html; charset=utf-8"));
            assertThat(actualSubscriberMimeMessage.getContentType(), equalTo("text/html; charset=utf-8"));

            if (actualSubscriberMimeMessage.getSubject().equals("New question was added on EXP Portal: "
                    + questionPostDto.getTitle())) {
                assertThat(actualCreatorMimeMessage.getSubject(),
                        equalTo("New question was added on EXP Portal by you: " + questionPostDto.getTitle()));
                assertThat(actualSubscriberMimeMessage.getSubject(),
                        equalTo("New question was added on EXP Portal: " + questionPostDto.getTitle()));
            }
            else {
                if (actualSubscriberMimeMessage.getSubject().
                        equals("[EPAM Experience Portal] A new answer has been added to the question > "
                                + questionPostDto.getTitle())) {
                    assertThat(actualCreatorMimeMessage.getSubject(),
                            equalTo("[EPAM Experience Portal] A new answer has been added to your question > "
                                    + questionPostDto.getTitle()));
                    assertThat(actualSubscriberMimeMessage.getSubject(),
                            equalTo("[EPAM Experience Portal] A new answer has been added to the question > "
                                    + questionPostDto.getTitle()));
                } else
                    fail("Wrong MimeMessages");
            }
            return null;
        }).when(mailService).sendMessages(any());

        addNewQuestionAndAnswerToItInDB(questionPostDto, questionAuthorEmail, answerAuthorEmail);

        verify(mailService, times(2)).sendMessages(any());
    }

    @Test
    public void whenNewAnswerWasAddedMailShouldBeSendToCorrectRecipients() throws Exception {

        Question savedQuestion = questionService.addNewQuestion(questionPostDto, questionAuthorEmail);
        AnswerPostDto answerPost = new AnswerPostDto(savedQuestion.getQuestionId(), "Text of Answer");

        doAnswer(invocation -> {
            LinkedList<MimeMessage> actualMimeMessageAnswerList = new LinkedList<>(
                    (Collection<MimeMessage>) invocation.getArguments()[0]);
            MimeMessage actualCreatorMimeMessage = actualMimeMessageAnswerList.get(0);
            MimeMessage actualSubscriberMimeMessage = actualMimeMessageAnswerList.get(1);

            assertThat(actualMimeMessageAnswerList.size(), equalTo(2));

            Address[] expectedSubscriberRecipients = {new InternetAddress("Tyler_Greeds@epam.com"),
                    new InternetAddress("Bob_Hoplins@epam.com"), new InternetAddress("Donald_Gardner@epam.com")};
            assertThat(actualSubscriberMimeMessage.getAllRecipients(), equalTo(expectedSubscriberRecipients));
            assertThat(actualSubscriberMimeMessage.getSubject(),
                    equalTo("[EPAM Experience Portal] A new answer has been added to the question > "
                            + questionPostDto.getTitle()));

            Address[] expectedCreatorRecipients = {new InternetAddress(answerAuthorEmail)};
            assertThat(actualCreatorMimeMessage.getAllRecipients(), equalTo(expectedCreatorRecipients));
            assertThat(actualCreatorMimeMessage.getSubject(),
                    equalTo("[EPAM Experience Portal] A new answer has been added to your question > "
                            + questionPostDto.getTitle()));

            return null;
        }).when(mailService).sendMessages(any());

        answerService.addNewAnswer(answerPost, answerAuthorEmail);

        verify(mailService, times(2)).sendMessages(any());
    }

    @Test
    public void whenNewAnswerWasAddedMailShouldNotBeSendToAnybodyIfQuestionHasNotSubscribers() throws Exception {

        QuestionPostDto questionWithoutSubs = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                someString(), someLong(),
                new ArrayList<>(), Collections.emptyList());
        Question savedQuestion = questionService.addNewQuestion(questionWithoutSubs, questionAuthorEmail);
        AnswerPostDto answerPost = new AnswerPostDto(savedQuestion.getQuestionId(), "Text of Answer");

        doAnswer(invocation -> {
            LinkedList<MimeMessage> actualMimeMessageAnswerList = new LinkedList<>(
                    (Collection<MimeMessage>) invocation.getArguments()[0]);

            assertThat(actualMimeMessageAnswerList.size(), equalTo(0));

            return null;
        }).when(mailService).sendMessages(any());

        answerService.addNewAnswer(answerPost, answerAuthorEmail);

        verify(mailService, times(2)).sendMessages(any());
    }

    @Test
    public void whenNewAnswerWasAddedMailShouldBeSendOnlyToAuthorIfQuestionHasNotAnotherSubscribers() throws Exception {

        QuestionPostDto questionWithoutSubs = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                someString(), someLong(),
                Collections.singletonList(answerAuthorEmail), Collections.emptyList());
        Question savedQuestion = questionService.addNewQuestion(questionWithoutSubs, questionAuthorEmail);
        AnswerPostDto answerPost = new AnswerPostDto(savedQuestion.getQuestionId(), "Text of Answer");

        doAnswer(invocation -> {
            LinkedList<MimeMessage> actualMimeMessageAnswerList = new LinkedList<>(
                    (Collection<MimeMessage>) invocation.getArguments()[0]);
            MimeMessage actualCreatorMimeMessage = actualMimeMessageAnswerList.get(0);

            assertThat(actualMimeMessageAnswerList.size(), equalTo(1));

            Address[] expectedRecipients = {new InternetAddress(answerAuthorEmail)};
            assertThat(actualCreatorMimeMessage.getAllRecipients(), equalTo(expectedRecipients));
            assertThat(actualCreatorMimeMessage.getSubject(),
                    equalTo("[EPAM Experience Portal] A new answer has been added to your question > "
                            + questionWithoutSubs.getTitle()));

            return null;
        }).when(mailService).sendMessages(any());

        answerService.addNewAnswer(answerPost, answerAuthorEmail);

        verify(mailService, times(2)).sendMessages(any());
    }

    @Test
    public void whenNewAnswerWasAddedMailShouldBeSendOnlyToSubscribersIfAuthorDoesNotSubscribed() throws Exception {

        QuestionPostDto questionWithoutSubs = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                someString(), someLong(),
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com"), Collections.emptyList());
        Question savedQuestion = questionService.addNewQuestion(questionWithoutSubs, questionAuthorEmail);
        AnswerPostDto answerPost = new AnswerPostDto(savedQuestion.getQuestionId(), "Text of Answer");

        doAnswer(invocation -> {
            LinkedList<MimeMessage> actualMimeMessageAnswerList = new LinkedList<>(
                    (Collection<MimeMessage>) invocation.getArguments()[0]);
            MimeMessage actualSubscriberMimeMessage = actualMimeMessageAnswerList.get(0);

            assertThat(actualMimeMessageAnswerList.size(), equalTo(1));

            Address[] expectedRecipients = {new InternetAddress("Tyler_Greeds@epam.com"),
                    new InternetAddress("Bob_Hoplins@epam.com"), new InternetAddress("Donald_Gardner@epam.com")};
            assertThat(actualSubscriberMimeMessage.getAllRecipients(), equalTo(expectedRecipients));
            assertThat(actualSubscriberMimeMessage.getSubject(),
                    equalTo("[EPAM Experience Portal] A new answer has been added to the question > "
                            + questionWithoutSubs.getTitle()));

            return null;
        }).when(mailService).sendMessages(any());

        answerService.addNewAnswer(answerPost, answerAuthorEmail);

        verify(mailService, times(2)).sendMessages(any());
    }

    private void addNewQuestionAndAnswerToItInDB(QuestionPostDto question, String questionAuthor, String answerAuthor) {

        Question savedQuestion = questionService.addNewQuestion(question, questionAuthor);
        AnswerPostDto answerPost = new AnswerPostDto(savedQuestion.getQuestionId(), "Text of Answer");
        answerService.addNewAnswer(answerPost, answerAuthor);
    }
}
