package com.epam.lstrsum.email.template;

import com.epam.lstrsum.email.EmailCollection;
import com.epam.lstrsum.model.Question;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Component
@Profile("email")
@RequiredArgsConstructor
@Slf4j
public class NewQuestionNotificationTemplate implements MailTemplate<Question> {

    private final EmailCollection<Question> emailCollection;
    @Autowired
    private final TemplateEngine templateEngine;

    @Setter
    @Value("${spring.mail.default-question-link}")
    private String defaultQuestionLink;

    @Setter
    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public Collection<MimeMessage> buildMailMessages(Question question, boolean fromPortal) throws MessagingException {
        List<MimeMessage> mimeMessageCollection = new LinkedList<>();

        String questionPath = defaultQuestionLink + question.getQuestionId();
        log.debug("Building message, question path: {}", questionPath);

        Context context = new Context();
        context.setVariable("questionText", question.getText());
        context.setVariable("questionDeadline", question.getDeadLine());
        context.setVariable("questionPath", questionPath);

        Address[] addressesToNotify;
        if (fromPortal)
            addressesToNotify = getAddressesToNotifyFromPortal(question);
        else
            addressesToNotify = getAddressesToNotifyFromEmail(question);

        Address creatorAddress = new InternetAddress(question.getAuthorId().getEmail());
        List<Address> addressesToNotifyList = new LinkedList<>(Arrays.asList(addressesToNotify));
        addressesToNotifyList.remove(creatorAddress);
        addressesToNotify = addressesToNotifyList.toArray(new Address[addressesToNotifyList.size()]);

        mimeMessageCollection.add(buildMimeMessage(question, context, creatorAddress,
                addressesToNotify, true));

        if (addressesToNotify.length > 0)
            mimeMessageCollection.add(buildMimeMessage(question, context, creatorAddress,
                    addressesToNotify, false));

        return mimeMessageCollection;
    }

    private MimeMessage buildMimeMessage(Question question, Context context, Address creatorAddress,
                                         Address[] addressesToNotify, boolean isItMailToCreator)
            throws MessagingException {

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        mimeMessage.setFrom(new InternetAddress(fromAddress));
        if (isItMailToCreator) {
            mimeMessage.setSubject("New question was added on EXP Portal by you: " + question.getTitle());
            mimeMessage.setText(templateEngine.process("newQuestionCreator", context),
                    "utf-8","html");
            mimeMessage.setRecipient(Message.RecipientType.TO, creatorAddress);
        }
        else {
            mimeMessage.setSubject("New question was added on EXP Portal: " + question.getTitle());
            mimeMessage.setText(templateEngine.process("newQuestionSubscriber", context),
                    "utf-8", "html");
            mimeMessage.setRecipients(Message.RecipientType.TO, addressesToNotify);
        }
        mimeMessage.saveChanges();
        return mimeMessage;
    }

    private Address[] getAddressesToNotifyFromEmail(Question source) {
        return emailCollection.getEmailAddressesToNotifyFromEmail(source);
    }

    private Address[] getAddressesToNotifyFromPortal(Question source) {
        return emailCollection.getEmailAddressesToNotifyFromPortal(source);
    }
}