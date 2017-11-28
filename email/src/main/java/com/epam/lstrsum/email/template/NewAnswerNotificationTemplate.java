package com.epam.lstrsum.email.template;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.email.EmailCollection;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@Component
@Profile("email")
@RequiredArgsConstructor
@Slf4j
public class NewAnswerNotificationTemplate implements MailTemplate<AnswerAllFieldsDto> {

    @Setter
    @Value("${spring.mail.default-question-link}")
    private String defaultQuestionLink;

    private final EmailCollection<AnswerAllFieldsDto> emailCollection;

    @Setter
    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public MimeMessage buildMailMessage(AnswerAllFieldsDto source, boolean fromPortal) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        mimeMessage.setFrom(new InternetAddress(fromAddress));
        mimeMessage.setSubject(getSubject(source));
        mimeMessage.setContent(getContentOfMessage(source));
        mimeMessage.setRecipients(Message.RecipientType.TO,
                fromPortal ? getAddressesToNotifyFromPortal(source) : getAddressesToNotifyFromEmail(source));

        mimeMessage.saveChanges();

        return mimeMessage;
    }

    private Address[] getAddressesToNotifyFromEmail(AnswerAllFieldsDto source) {
        return emailCollection.getEmailAddressesToNotifyFromEmail(source);
    }

    private Address[] getAddressesToNotifyFromPortal(AnswerAllFieldsDto source) {
        return emailCollection.getEmailAddressesToNotifyFromPortal(source);
    }

    private String getSubject(AnswerAllFieldsDto source) {
        return "[EPAM Experience Portal] A new answer has been added to the question > " +
                source.getQuestion().getTitle();
    }

    private Multipart getContentOfMessage(AnswerAllFieldsDto source) throws MessagingException {
        Multipart parts = new MimeMultipart();

        MimeBodyPart plainText = new MimeBodyPart();
        plainText.setText(getTextMessage(source), "utf-8", "html");

        parts.addBodyPart(plainText);

        return parts;
    }

    private String getTextMessage(AnswerAllFieldsDto source) {
        String questionPath = defaultQuestionLink + source.getQuestion().getQuestionId();

        log.debug("New answer on question: {}", questionPath);

        return source.getAuthor().getFirstName() + " " +
                source.getAuthor().getLastName() +
                " has posted the following answer:<br>" +
                source.getText() +
                "<br>" +
                "<a href=\"" + questionPath + "\">Go to answer</a>";
    }

    private String getHyperLinkMessage(AnswerAllFieldsDto source) {
        String questionPath = defaultQuestionLink + source.getQuestion().getQuestionId();

        String link = "<a href=\"" + questionPath + "\">here</a>";

        return "More details on the answer could be found " +
                link;
    }
}
