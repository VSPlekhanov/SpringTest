package com.epam.lstrsum.email.template;

import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.email.EmailCollection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Objects;
import java.util.Set;

@Component
@Profile("email")
@Slf4j
public class NewQuestionNotificationTemplate implements MailTemplate<QuestionAllFieldsDto> {

    private final EmailCollection<QuestionAllFieldsDto> emailCollection;

    private static final String MAIL_HEADER = "\nHello!\n\nA new question was added to EXP Portal!\n\n";

    @Autowired
    @SuppressWarnings("all")
    public NewQuestionNotificationTemplate(EmailCollection<QuestionAllFieldsDto> emailCollection) {
        this.emailCollection = emailCollection;
    }

    @Override
    public MimeMessage buildMailMessage(QuestionAllFieldsDto question) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        mimeMessage.setSubject("New question was added on EXP Portal: " + question.getTitle());
        mimeMessage.setText(MAIL_HEADER + question.getText() + "\n\n" + "Deadline: " + question.getDeadLine());

        mimeMessage.setRecipients(Message.RecipientType.TO, getAddresses(question));
        return mimeMessage;
    }

    private Address[] getAddresses(QuestionAllFieldsDto question) {
        Set<String> emails = emailCollection.getEmails(question);
        return emails.stream()
                .map((s) -> {
                    try {
                        return new InternetAddress(s);
                    } catch (Exception e) {
                        log.warn("Could not parse email address: " + s);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(Address[]::new);
    }
}
