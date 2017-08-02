package com.epam.lstrsum.mail;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.mail.service.MailService;
import com.epam.lstrsum.mail.template.NewQuestionNotificationTemplate;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.SubscriptionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest("spring.profiles.active=email")
public class EmailNotificationAspectTest extends SetUpDataBaseCollections {

    @Autowired
    private NewQuestionNotificationTemplate template;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private SubscriptionService subscriptionService;

    @MockBean
    private MailService mailService;

    @Autowired
    private EmailNotificationAspect aspect;

    @Test
    public void whenNewRequestAddedMailWithExpectedSubjectShouldBeSent() throws Exception {
        String authorEmail = "John_Doe@epam.com";
        QuestionPostDto postDto = new QuestionPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", 11223344L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));

        QuestionAllFieldsDto savedDto = questionService.addNewQuestion(postDto, authorEmail);

        MimeMessage expected = template.buildMailMessage(savedDto);

        doAnswer((i) -> {
            MimeMessage actual = (MimeMessage) i.getArguments()[0];
            assertThat(actual.getSubject(), is(expected.getSubject()));
            return null;
        }).when(mailService).sendMessage(any());
    }

    @Test
    public void whenNewRequestAddedNotificationShouldBeSentToCorrectMailingList() throws Exception {
        String authorEmail = "John_Doe@epam.com";
        QuestionPostDto postDto = new QuestionPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", 11223344L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));

        QuestionAllFieldsDto savedDto = questionService.addNewQuestion(postDto, authorEmail);

        MimeMessage expected = template.buildMailMessage(savedDto);

        List<String> expectedEmails = subscriptionService.getEmailsToNotificateAboutNewQuestion(savedDto.getQuestionId());

        doAnswer((i) -> {
            MimeMessage actual = (MimeMessage) i.getArguments()[0];
            Address[] recipients = actual.getRecipients(Message.RecipientType.TO);
            List<String> actualEmails = Stream.of(recipients).map(Address::toString).collect(Collectors.toList());
            assertThat(actualEmails, is(expectedEmails));
            return null;
        }).when(mailService).sendMessage(any());
    }

    @Test
    public void whenRequestServiceThrowExceptionNotificationShouldNotBeSent() throws Exception {
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
