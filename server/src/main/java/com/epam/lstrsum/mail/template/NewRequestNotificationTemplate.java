package com.epam.lstrsum.mail.template;

import com.epam.lstrsum.dto.request.RequestAllFieldsDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
@Profile("email")
@Slf4j
public class NewRequestNotificationTemplate implements MailTemplate<RequestAllFieldsDto> {

    private final SubscriptionService subscriptionService;

    private static final String MAIL_HEADER = "\nHello!\n\nA new request was added to EXP Portal!\n\n";

    @Autowired
    public NewRequestNotificationTemplate(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public MimeMessage buildMailMessage(RequestAllFieldsDto source) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        mimeMessage.setSubject("New request was added on EXP Portal: " + source.getTitle());
        mimeMessage.setText(MAIL_HEADER + source.getText() + "\n\n" + "Deadline: " + source.getDeadLine());

        mimeMessage.setRecipients(Message.RecipientType.TO, getAddresses(source));
        return mimeMessage;
    }

    private Address[] getAddresses(RequestAllFieldsDto source) {
        List<String> emails = subscriptionService.getEmailsToNotificateAboutNewRequest(source.getRequestId());
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
