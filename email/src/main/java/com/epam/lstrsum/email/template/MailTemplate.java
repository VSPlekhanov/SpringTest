package com.epam.lstrsum.email.template;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Collection;

/**
 * Implement this interface to determine the way of
 * constructing MimeMessage from custom object.
 * <p>
 * Used to construct notification emails from DTOs on
 * creating, removing, or updating operations.
 *
 * @param <T> Source for building MimeMessage
 */
public interface MailTemplate<T> {
    Collection<MimeMessage> buildMailMessages(T source, boolean fromPortal) throws MessagingException;
}
