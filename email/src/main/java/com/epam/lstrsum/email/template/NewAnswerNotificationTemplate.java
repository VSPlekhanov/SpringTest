package com.epam.lstrsum.email.template;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.email.EmailCollection;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@Component
@Profile("email")
@ConfigurationProperties(prefix = "email")
@RequiredArgsConstructor
public class NewAnswerNotificationTemplate implements MailTemplate<AnswerAllFieldsDto> {

    @Setter
    private static String defaultQuestionLink;

    private final EmailCollection<AnswerAllFieldsDto> emailCollection;

    @Override
    public MimeMessage buildMailMessage(AnswerAllFieldsDto source) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        mimeMessage.setSubject(getSubject(source));
        mimeMessage.setContent(getContentOfMessage(source));
        mimeMessage.setRecipients(Message.RecipientType.TO, getAddresses(source));

        mimeMessage.saveChanges();

        return mimeMessage;
    }

    private Address[] getAddresses(AnswerAllFieldsDto source) {
        return emailCollection.getEmailAddresses(source);
    }

    private String getSubject(AnswerAllFieldsDto source) {
        return "[EPAM Experience Portal] A new answer has been added to the question > " +
                source.getQuestionId().getTitle();
    }

    private Multipart getContentOfMessage(AnswerAllFieldsDto source) throws MessagingException {
        Multipart parts = new MimeMultipart();

        MimeBodyPart plainText = new MimeBodyPart();
        plainText.setText(getTextMessage(source), "utf-8");

        MimeBodyPart hyperLink = new MimeBodyPart();
        hyperLink.setContent(getHyperLinkMessage(source), "text/html; charset=utf-8");

        parts.addBodyPart(plainText);
        parts.addBodyPart(hyperLink);

        return parts;
    }

    private String getTextMessage(AnswerAllFieldsDto source) {
        return source.getAuthorId().getFirstName() + " " +
                source.getAuthorId().getLastName() +
                " has posted the following answer:\n\n" +
                source.getText() +
                "\n\n";
    }

    private String getHyperLinkMessage(AnswerAllFieldsDto source) {
        String questionPath = defaultQuestionLink + source.getQuestionId().getQuestionId();
        String link = "<a href=\"" + questionPath + "\">here</a>";

        return "More details on the answer could be found " +
                link;
    }
}
