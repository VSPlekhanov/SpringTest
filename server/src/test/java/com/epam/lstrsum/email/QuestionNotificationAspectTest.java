package com.epam.lstrsum.email;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.email.service.MailService;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.QuestionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.lstrsum.testutils.InstantiateUtil.someLong;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("email")
public class QuestionNotificationAspectTest extends SetUpDataBaseCollections {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private EmailCollection<Question> questionEmailCollection;

    @MockBean
    private MailService mailService;

    @Autowired
    private EmailNotificationAspect aspect;

    @SuppressWarnings("unchecked")
    @Test
    public void whenNewQuestionAddedMailWithExpectedSubjectAndBodyTypeShouldBeSend() throws Exception {

        String questionAuthor = "John_Doe@epam.com";
        QuestionPostDto questionPostDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                someString(), someLong(),
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"), Collections.emptyList());

        doAnswer(invocation -> {
            LinkedList<MimeMessage> actualMimeMessageList = new LinkedList<>(
                    (Collection<MimeMessage>) invocation.getArguments()[0]);
            MimeMessage actualCreatorMimeMessage = actualMimeMessageList.get(0);
            MimeMessage actualSubscriberMimeMessage = actualMimeMessageList.get(1);

            assertThat(actualMimeMessageList.size(), equalTo(2));

            assertThat(actualCreatorMimeMessage.getContentType(), equalTo("text/html; charset=utf-8"));
            assertThat(actualSubscriberMimeMessage.getContentType(), equalTo("text/html; charset=utf-8"));

            assertThat(actualCreatorMimeMessage.getSubject(),
                    equalTo("New question was added on EXP Portal by you: " + questionPostDto.getTitle()));
            assertThat(actualSubscriberMimeMessage.getSubject(),
                    equalTo("New question was added on EXP Portal: " + questionPostDto.getTitle()));
            return null;
        }).when(mailService).sendMessages(any());

        questionService.addNewQuestion(questionPostDto, questionAuthor);

        verify(mailService, times(1)).sendMessages(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenNewQuestionWasAddedMailShouldBeSendToCorrectRecipients() throws Exception {

        String questionAuthor = "John_Doe@epam.com";
        QuestionPostDto questionPostDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                someString(), someLong(),
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"), Collections.emptyList());

        doAnswer(invocation -> {
            LinkedList<MimeMessage> actualMimeMessageAnswerList = new LinkedList<>(
                    (Collection<MimeMessage>) invocation.getArguments()[0]);
            MimeMessage actualCreatorMimeMessage = actualMimeMessageAnswerList.get(0);

            assertThat(actualMimeMessageAnswerList.size(), IsEqual.equalTo(2));

            Address[] expectedCreatorRecipients = {new InternetAddress(questionAuthor)};
            assertThat(actualCreatorMimeMessage.getAllRecipients(), IsEqual.equalTo(expectedCreatorRecipients));
            assertThat(actualCreatorMimeMessage.getSubject(),
                    IsEqual.equalTo("New question was added on EXP Portal by you: "
                            + questionPostDto.getTitle()));

            return null;
        }).when(mailService).sendMessages(any());

        questionService.addNewQuestion(questionPostDto, questionAuthor);

        verify(mailService, times(1)).sendMessages(any());
    }

    @Test
    public void whenNewQuestionAddedFromPortalMailShouldBeSentOnce() throws Exception {
        whenNewQuestionAddedMailShouldBeSentOnce(questionService::addNewQuestion);
    }

    @Test
    public void whenNewQuestionAddedFromEmailMailShouldBeSentOnce() throws Exception {
        whenNewQuestionAddedMailShouldBeSentOnce(questionService::addNewQuestionFromEmail);
    }

    public void whenNewQuestionAddedMailShouldBeSentOnce(BiFunction<QuestionPostDto, String, Question> addQuestion) throws Exception {
        String authorEmail = "John_Doe@epam.com";
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 11223344L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"), emptyList());

        addQuestion.apply(postDto, authorEmail);
        verify(mailService, times(1)).sendMessages(any());
    }

    @Test
    public void whenNewQuestionAddedFromPortalNotificationShouldBeSentToCorrectMailingList() throws Exception {
        whenNewQuestionAddedNotificationShouldBeSentToCorrectMailingList(
                questionService::addNewQuestion, questionEmailCollection::getEmailAddressesToNotifyFromPortal);
    }

    @Test
    public void whenNewQuestionAddedFromEmailNotificationShouldBeSentToCorrectMailingList() throws Exception {
        whenNewQuestionAddedNotificationShouldBeSentToCorrectMailingList(
                questionService::addNewQuestionFromEmail, questionEmailCollection::getEmailAddressesToNotifyFromEmail);
    }

    public void whenNewQuestionAddedNotificationShouldBeSentToCorrectMailingList(
            BiFunction<QuestionPostDto, String, Question> addQuestion,
            Function<Question, Address[]> getEmails) throws Exception {
        String authorEmail = "John_Doe@epam.com";
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 11223344L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"), emptyList());

        Question savedDto = addQuestion.apply(postDto, authorEmail);
        Address[] expectedEmails = getEmails.apply(savedDto);

        verify(mailService).sendMessages(any());

        doAnswer((i) -> {
            MimeMessage actual = (MimeMessage) i.getArguments()[0];
            Address[] recipients = actual.getRecipients(Message.RecipientType.TO);
            List<String> actualEmails = Stream.of(recipients).map(Address::toString).collect(Collectors.toList());
            assertThat(actualEmails, is(expectedEmails));
            return null;
        }).when(mailService).sendMessages(any());
    }

    @Test
    public void whenQuestionServiceFromPortalThrowExceptionNotificationShouldNotBeSent() throws Exception {
        try {
            questionService.addNewQuestion(null, "");
        } catch (Exception e) {
            verify(mailService, never()).sendMessages(any());
        }
    }

    @Test
    public void whenQuestionServiceFromEmailThrowExceptionNotificationShouldNotBeSent() throws Exception {
        try {
            questionService.addNewQuestionFromEmail(null, "");
        } catch (Exception e) {
            verify(mailService, never()).sendMessages(any());
        }
    }

    @Test
    public void whenMailTemplateThrowExceptionNoExceptionShouldBeThrownFromAspect() throws Throwable {
        ProceedingJoinPoint mock = mock(ProceedingJoinPoint.class);
        when(mock.proceed()).thenReturn(null);

        aspect.aroundAdvice(mock);
    }

}
