package com.epam.lstrsum.email.template;

import com.epam.lstrsum.dto.feedback.FeedbackAllFieldsDto;
import com.epam.lstrsum.model.Attachment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Component
@Profile("email")
@RequiredArgsConstructor
@Slf4j
public class NewFeedbackNotificationTemplate implements MailTemplate<FeedbackAllFieldsDto> {

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("#{'${spring.mail.feedback.emails}'.split(',')}")
    private List<String> toAddresses;

    @Override
    public Collection<MimeMessage> buildMailMessages(FeedbackAllFieldsDto source, boolean fromPortal) throws MessagingException {
        List<MimeMessage> mimeMessageCollection = new LinkedList<>();
        mimeMessageCollection.add(buildMimeMessage(source, fromPortal));
        return mimeMessageCollection;
    }

    private MimeMessage buildMimeMessage(FeedbackAllFieldsDto source, boolean fromPortal) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        mimeMessage.setFrom(new InternetAddress(fromAddress));
        mimeMessage.setSubject("New feedback was added on EXP Portal: " + source.getTitle());
        mimeMessage.setContent(getContentOfMessage(source));
        mimeMessage.setRecipients(Message.RecipientType.TO, getAddressesFromEmails(toAddresses));

        mimeMessage.saveChanges();

        return mimeMessage;
    }

    private Multipart getContentOfMessage(FeedbackAllFieldsDto source) throws MessagingException {
        Multipart parts = new MimeMultipart();

        MimeBodyPart plainText = new MimeBodyPart();
        plainText.setText(source.getText(), "utf-8");

        parts.addBodyPart(plainText);

        List<Attachment> attachmentList = source.getAttachments();
        for (Attachment attachment : attachmentList) {
            MimeBodyPart filePart = new MimeBodyPart();

            ByteArrayDataSource bads =
                    new ByteArrayDataSource(attachment.getData(), attachment.getType());
            filePart.setDataHandler(new DataHandler(bads));
            filePart.setFileName(attachment.getName());
            parts.addBodyPart(filePart);
        }


        return parts;
    }

    private static Address[] getAddressesFromEmails(Collection<String> emails) {
        return emails.stream()
                .map((s) -> {
                    try {
                        return new InternetAddress(s);
                    } catch (Exception e) {
                        log.warn("Could not parse email address: {} {}", s, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(Address[]::new);
    }
}