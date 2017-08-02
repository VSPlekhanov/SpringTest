package com.epam.lstrsum.mail.template;

import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.service.SubscriptionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class NewQuestionNotificationTemplateTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void questionNotificationTemplateShouldNotTakeNotCorrectEmails() throws Exception {
        NewQuestionNotificationTemplate template = new NewQuestionNotificationTemplate(subscriptionService);

        UserBaseDto userBaseDto = new UserBaseDto("uid", "Vasya",
                "Pupkin", "(invalid_mail");

        String id = "id";
        List<UserBaseDto> allowedSubs = Collections.singletonList(userBaseDto);
        QuestionAllFieldsDto questionAllFieldsDto = new QuestionAllFieldsDto(id, "title", new String[]{"tag"}, Instant.now(),
                Instant.now(), userBaseDto, 0, allowedSubs, "text");

        String validMail = "valid@mail.dom";
        when(subscriptionService.getEmailsToNotificateAboutNewQuestion(id))
                .thenReturn(Arrays.asList(allowedSubs.get(0).getEmail(), validMail));

        MimeMessage mimeMessage = template.buildMailMessage(questionAllFieldsDto);

        Address[] recipients = mimeMessage.getRecipients(Message.RecipientType.TO);

        assertThat(recipients, is(new InternetAddress[]{new InternetAddress(validMail)}));
    }

}