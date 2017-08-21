package com.epam.lstrsum.email.template;

import com.epam.lstrsum.email.EmailCollection;
import com.epam.lstrsum.model.Question;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;

@Component
@Profile("email")
@RequiredArgsConstructor
public class NewQuestionNotificationTemplate implements MailTemplate<Question> {

    private static final String MAIL_HEADER = "\nHello!\n\nA new question was added to EXP Portal!\n\n";
    private final EmailCollection<Question> emailCollection;

    @Setter
    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public MimeMessage buildMailMessage(Question question) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        mimeMessage.setFrom(new InternetAddress(fromAddress));
        mimeMessage.setSubject("New question was added on EXP Portal: " + question.getTitle());
        mimeMessage.setText(MAIL_HEADER + question.getText() + "\n\n" + "Deadline: " + question.getDeadLine());

        mimeMessage.setRecipients(Message.RecipientType.TO, getAddresses(question));
        return mimeMessage;
    }

    private Address[] getAddresses(Question source) {
        return Arrays.stream(emailCollection.getEmailAddresses(source))
                .toArray(Address[]::new);
    }
}
