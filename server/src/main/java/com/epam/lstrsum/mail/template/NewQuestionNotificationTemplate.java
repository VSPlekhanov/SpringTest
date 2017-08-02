package com.epam.lstrsum.mail.template;

import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.service.SubscriptionService;
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
import java.util.List;
import java.util.Objects;

@Component
@Profile("email")
@Slf4j
public class NewQuestionNotificationTemplate implements MailTemplate<QuestionAllFieldsDto> {

    private final SubscriptionService subscriptionService;

    private static final String MAIL_HEADER = "\nHello!\n\nA new question was added to EXP Portal!\n\n";

    @Autowired
    public NewQuestionNotificationTemplate(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public MimeMessage buildMailMessage(QuestionAllFieldsDto source) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        mimeMessage.setSubject("New question was added on EXP Portal: " + source.getTitle());
        mimeMessage.setText(MAIL_HEADER + source.getText() + "\n\n" + "Deadline: " + source.getDeadLine());

        mimeMessage.setRecipients(Message.RecipientType.TO, getAddresses(source));
        return mimeMessage;
    }

    private Address[] getAddresses(QuestionAllFieldsDto source) {
        List<String> emails = subscriptionService.getEmailsToNotificateAboutNewQuestion(source.getQuestionId());
        return emails.stream()
                .map((s) -> {
                    try {
                        return new InternetAddress(s);
                    } catch (Exception e) {
                        log.warn("Could not parse email address: " + s, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(Address[]::new);
    }
}
