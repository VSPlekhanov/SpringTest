package com.epam.lstrsum.email.template;

import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.email.EmailCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

@Component
@Profile("email")
@RequiredArgsConstructor
public class NewQuestionNotificationTemplate implements MailTemplate<QuestionAllFieldsDto> {

    private final EmailCollection<QuestionAllFieldsDto> emailCollection;

    private static final String MAIL_HEADER = "\nHello!\n\nA new question was added to EXP Portal!\n\n";

    @Override
    public MimeMessage buildMailMessage(QuestionAllFieldsDto question) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        mimeMessage.setSubject("New question was added on EXP Portal: " + question.getTitle());
        mimeMessage.setText(MAIL_HEADER + question.getText() + "\n\n" + "Deadline: " + question.getDeadLine());

        mimeMessage.setRecipients(Message.RecipientType.TO, getAddresses(question));
        return mimeMessage;
    }

    private Address[] getAddresses(QuestionAllFieldsDto source) {
        return emailCollection.getEmailAddresses(source);
    }
}
