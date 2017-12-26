package com.epam.lstrsum.email.template;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.email.EmailCollection;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

@Component
@Profile("email")
@RequiredArgsConstructor
@Slf4j
public class NewAnswerNotificationTemplate implements MailTemplate<AnswerAllFieldsDto> {

    @Setter
    @Value("${spring.mail.default-question-link}")
    private String defaultQuestionLink;

    private final EmailCollection<AnswerAllFieldsDto> emailCollection;
    @Autowired
    private final TemplateEngine templateEngine;

    @Setter
    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public Collection<MimeMessage> buildMailMessages(AnswerAllFieldsDto source, boolean fromPortal)
            throws MessagingException {
        List<MimeMessage> mimeMessageCollection = new LinkedList<>();

        Address[] addressesToNotify;
        if (fromPortal)
            addressesToNotify = getAddressesToNotifyFromPortal(source);
        else
            addressesToNotify = getAddressesToNotifyFromEmail(source);

        List<Address> addressesToNotifyList = new LinkedList<>(Arrays.asList(addressesToNotify));
        Address creatorAddress = searchCreatorAddressInAddressesToNotifyListAndRemoveItIfContains(source,
                addressesToNotifyList);

        if (Objects.nonNull(creatorAddress)) {
            addressesToNotify = addressesToNotifyList.toArray(new Address[addressesToNotifyList.size()]);
            mimeMessageCollection.add(buildMimeMessage(source, creatorAddress, addressesToNotify));
        }

        if (addressesToNotify.length > 0)
            mimeMessageCollection.add(buildMimeMessage(source, null, addressesToNotify));

        return mimeMessageCollection;
    }

    private Address[] getAddressesToNotifyFromEmail(AnswerAllFieldsDto source) {
        return emailCollection.getEmailAddressesToNotifyFromEmail(source);
    }

    private Address[] getAddressesToNotifyFromPortal(AnswerAllFieldsDto source) {
        return emailCollection.getEmailAddressesToNotifyFromPortal(source);
    }

    private MimeMessage buildMimeMessage(
            AnswerAllFieldsDto source, Address creatorAddress, Address[] addressesToNotify) throws MessagingException {

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        mimeMessage.setFrom(new InternetAddress(fromAddress));
        mimeMessage.setSubject(getSubject(source, creatorAddress));
        mimeMessage.setText(getTextMessage(source, creatorAddress), "utf-8", "html");
        if (Objects.nonNull(creatorAddress))
            mimeMessage.setRecipient(Message.RecipientType.TO, creatorAddress);
        else
            mimeMessage.setRecipients(Message.RecipientType.TO, addressesToNotify);
        mimeMessage.saveChanges();
        return mimeMessage;
    }

    private Address searchCreatorAddressInAddressesToNotifyListAndRemoveItIfContains (
            AnswerAllFieldsDto source, List<Address> addressesToNotify) throws AddressException {

        Address creatorAddress = new InternetAddress(source.getQuestion().getAuthor().getEmail());
        if (addressesToNotify.remove(creatorAddress))
            return creatorAddress;
        else
            return null;
    }

    private String getSubject(AnswerAllFieldsDto source, Address creatorAddress) {
        if (Objects.nonNull(creatorAddress))
            return "[EPAM Experience Portal] A new answer has been added to your question > " +
                    source.getQuestion().getTitle();
        else
            return "[EPAM Experience Portal] A new answer has been added to the question > " +
                    source.getQuestion().getTitle();
    }

    private String getTextMessage(AnswerAllFieldsDto source, Address creatorAddress) {
        String questionPath = defaultQuestionLink + source.getQuestion().getQuestionId();

        if (Objects.nonNull(creatorAddress))
            log.debug("New answer (to creator) on question: {}", questionPath);
        else
            log.debug("New answer (to subscriber) on question: {}", questionPath);

        Context context = new Context();
        context.setVariable("authorFirstName", source.getAuthor().getFirstName());
        context.setVariable("authorLastName", source.getAuthor().getLastName());
        context.setVariable("messageText", source.getText());
        context.setVariable("questionPath", questionPath);
        if (Objects.nonNull(creatorAddress))
            return templateEngine.process("newAnswerCreator", context);
        else
            return templateEngine.process("newAnswerSubscriber", context);
    }
}
