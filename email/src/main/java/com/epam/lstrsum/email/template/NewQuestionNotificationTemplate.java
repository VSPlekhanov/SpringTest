package com.epam.lstrsum.email.template;

import com.epam.lstrsum.email.EmailCollection;
import com.epam.lstrsum.model.Question;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class NewQuestionNotificationTemplate implements MailTemplate<Question> {

    private static final String MAIL_HEADER = "<br>Hello!<br>A new question was added to EXP Portal!<br>";
    private final EmailCollection<Question> emailCollection;

    @Setter
    @Value("${spring.mail.default-question-link}")
    private String defaultQuestionLink;

    @Setter
    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public MimeMessage buildMailMessage(Question question) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        String questionPath = defaultQuestionLink + question.getQuestionId();

        log.debug("Building message, question path: " + questionPath);

        mimeMessage.setFrom(new InternetAddress(fromAddress));
        mimeMessage.setSubject("New question was added on EXP Portal: " + question.getTitle());
        mimeMessage.setText(MAIL_HEADER + question.getText() + "<br>" +
                "Deadline: " + question.getDeadLine() + "<br>" +
                "<a href=\"" + questionPath + "\">Go to question</a>",
                "utf-8",
                "html");

        mimeMessage.setRecipients(Message.RecipientType.TO, getAddresses(question));
        return mimeMessage;
    }

    private Address[] getAddresses(Question source) {
        return Arrays.stream(emailCollection.getEmailAddresses(source))
                .toArray(Address[]::new);
    }
}