package com.epam.lstrsum.mail.template;

import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.mail.EmailCollection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.HashSet;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class NewQuestionNotificationTemplateTest {

    @Mock
    private EmailCollection<QuestionAllFieldsDto> emailCollection;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void questionNotificationTemplateShouldNotTakeNotCorrectEmails() throws Exception {
        NewQuestionNotificationTemplate template = new NewQuestionNotificationTemplate(emailCollection);

        String invalidEmail = "(invalid_mail";
        String validMail = "valid@mail.dom";

        QuestionAllFieldsDto questionAllFieldsDto = createAllFieldsDto();

        when(emailCollection.getEmails(questionAllFieldsDto))
                .thenReturn(new HashSet<String>(){{add(invalidEmail); add(validMail);}});

        MimeMessage mimeMessage = template.buildMailMessage(questionAllFieldsDto);

        Address[] recipients = mimeMessage.getRecipients(Message.RecipientType.TO);

        assertThat(recipients, is(new InternetAddress[]{new InternetAddress(validMail)}));
    }

    private QuestionAllFieldsDto createAllFieldsDto() {
        return new QuestionAllFieldsDto("id", "title", new String[]{"tag"}, Instant.now(),
                Instant.now(), null, 0, null, "text");
    }

}