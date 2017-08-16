package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.impl.SubscriptionServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SubscriptionServiceUnitTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private AnswerAllFieldsDto answer;

    @Mock
    private Question question;

    @Before
    public void intiMocks() throws Exception {
        initMocks(this);
    }

    @Test
    public void questionEmailCollectionAdapterShouldNotTakeIncorrectEmails() throws Exception {
        String incorrectEmail = "(incorrect";
        String correctEmail = "correct@mail.com";

        SubscriptionServiceImpl.AnswerEmailCollectionAdapter adapter =
                new SubscriptionServiceImpl.AnswerEmailCollectionAdapter(subscriptionService);

        when(subscriptionService.getEmailsToNotificateAboutNewAnswer(any()))
                .thenReturn(new HashSet<String>() {{
                    add(incorrectEmail);
                    add(correctEmail);
                }});

        when(answer.getQuestionId()).thenReturn(new QuestionBaseDto("question_id",
                null, null, null, null, null, null));

        Address[] emailAddresses = adapter.getEmailAddresses(answer);

        assertThat(emailAddresses.length, equalTo(1));
        assertThat(emailAddresses[0], equalTo(new InternetAddress(correctEmail)));
    }

    @Test
    public void answerEmailCollectionAdapterShouldNotTakeIncorrectEmails() throws Exception {
        String incorrectEmail = "(incorrect";
        String correctEmail = "correct@mail.com";

        SubscriptionServiceImpl.QuestionEmailCollectionAdapter adapter =
                new SubscriptionServiceImpl.QuestionEmailCollectionAdapter(subscriptionService);

        when(subscriptionService.getEmailsToNotificateAboutNewQuestion(any()))
                .thenReturn(new HashSet<String>() {{
                    add(incorrectEmail);
                    add(correctEmail);
                }});

        when(question.getQuestionId()).thenReturn("question_id");

        Address[] emailAddresses = adapter.getEmailAddresses(question);

        assertThat(emailAddresses.length, equalTo(1));
        assertThat(emailAddresses[0], equalTo(new InternetAddress(correctEmail)));
    }
}
