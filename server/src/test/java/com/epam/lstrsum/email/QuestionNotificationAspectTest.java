package com.epam.lstrsum.email;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.email.service.MailService;
import com.epam.lstrsum.email.template.NewQuestionNotificationTemplate;
import com.epam.lstrsum.service.QuestionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.lstrsum.InstantiateUtil.someString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("email")
public class QuestionNotificationAspectTest extends SetUpDataBaseCollections {

    @Autowired
    private NewQuestionNotificationTemplate template;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private EmailCollection<QuestionAllFieldsDto> questionEmailCollection;

    @MockBean
    private MailService mailService;

    @Autowired
    private EmailNotificationAspect aspect;

    @Test
    public void whenNewQuestionAddedMailWithExpectedSubjectShouldBeSent() throws Exception {
        String authorEmail = "John_Doe@epam.com";
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 11223344L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));

        QuestionAllFieldsDto savedDto = questionService.addNewQuestion(postDto, authorEmail);

        MimeMessage expected = template.buildMailMessage(savedDto);

        verify(mailService).sendMessage(any());

        doAnswer((i) -> {
            MimeMessage actual = (MimeMessage) i.getArguments()[0];
            assertThat(actual.getSubject(), is(expected.getSubject()));
            return null;
        }).when(mailService).sendMessage(any());
    }

    @Test
    public void whenNewQuestionAddedMailShouldBeSentOnce() throws Exception {
        String authorEmail = "John_Doe@epam.com";
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 11223344L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));

        QuestionAllFieldsDto savedDto = questionService.addNewQuestion(postDto, authorEmail);

        MimeMessage expected = template.buildMailMessage(savedDto);

        verify(mailService, times(1)).sendMessage(any());
    }

    @Test
    public void whenNewQuestionAddedNotificationShouldBeSentToCorrectMailingList() throws Exception {
        String authorEmail = "John_Doe@epam.com";
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 11223344L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));

        QuestionAllFieldsDto savedDto = questionService.addNewQuestion(postDto, authorEmail);

        MimeMessage expected = template.buildMailMessage(savedDto);

        Address[] expectedEmails = questionEmailCollection.getEmailAddresses(savedDto);

        verify(mailService).sendMessage(any());

        doAnswer((i) -> {
            MimeMessage actual = (MimeMessage) i.getArguments()[0];
            Address[] recipients = actual.getRecipients(Message.RecipientType.TO);
            List<String> actualEmails = Stream.of(recipients).map(Address::toString).collect(Collectors.toList());
            assertThat(actualEmails, is(expectedEmails));
            return null;
        }).when(mailService).sendMessage(any());
    }

    @Test
    public void whenQuestionServiceThrowExceptionNotificationShouldNotBeSent() throws Exception {
        try {
            questionService.addNewQuestion(null, "");
        } catch (Exception e) {
            verify(mailService, never()).sendMessage(any());
        }
    }

    @Test
    public void whenMailTemplateThrowExceptionNoExceptionShouldBeThrownFromAspect() throws Throwable {
        ProceedingJoinPoint mock = mock(ProceedingJoinPoint.class);
        when(mock.proceed()).thenReturn(null);

        aspect.aroundAdvice(mock);
    }

}
