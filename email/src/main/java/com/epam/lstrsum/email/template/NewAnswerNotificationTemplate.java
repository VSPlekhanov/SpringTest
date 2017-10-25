package com.epam.lstrsum.email.template;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.email.EmailCollection;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Arrays;

@Component
@Profile("email")
@ConfigurationProperties(prefix = "email")
@RequiredArgsConstructor
public class NewAnswerNotificationTemplate implements MailTemplate<AnswerAllFieldsDto> {

    @Setter
    @Value("${spring.mail.defaultQuestionLink}")
    private static String defaultQuestionLink;
    private final EmailCollection<AnswerAllFieldsDto> emailCollection;

    @Setter
    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public MimeMessage buildMailMessage(AnswerAllFieldsDto source) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        mimeMessage.setFrom(new InternetAddress(fromAddress));
        mimeMessage.setSubject(getSubject(source));
        mimeMessage.setContent(getContentOfMessage(source));
        mimeMessage.setRecipients(Message.RecipientType.TO, getAddresses(source));

        mimeMessage.saveChanges();

        return mimeMessage;
    }

    private Address[] getAddresses(AnswerAllFieldsDto source) {
        return Arrays.stream(emailCollection.getEmailAddresses(source))
                .toArray(Address[]::new);
    }

    private String getSubject(AnswerAllFieldsDto source) {
        return "[EPAM Experience Portal] A new answer has been added to the question > " +
                source.getQuestionId().getTitle();
    }

    private Multipart getContentOfMessage(AnswerAllFieldsDto source) throws MessagingException {
        Multipart parts = new MimeMultipart();

        MimeBodyPart plainText = new MimeBodyPart();
        plainText.setText(getTextMessage(source), "utf-8", "html");

        parts.addBodyPart(plainText);

        return parts;
    }

    private String getTextMessage(AnswerAllFieldsDto source) {
        String questionPath = defaultQuestionLink + source.getQuestionId().getQuestionId();

        return source.getAuthorId().getFirstName() + " " +
                source.getAuthorId().getLastName() +
                " has posted the following answer:\n\n" +
                source.getText() +
                "\n\n" +
                "<a href=\"" + questionPath + "\">Go to answer</a>";
    }
}
