package com.epam.lstrsum.email.service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static java.util.Objects.isNull;

public class EmailParserUtil {
    public static String getReplyTo(MimeMessage mimeMessage) throws MessagingException {
        String[] replyTo = mimeMessage.getHeader("Reply-To");
        if (isNull(replyTo) || replyTo.length == 0) {
            return null;
        }
        return mimeMessage.getHeader("Reply-To")[0];
    }

    public static String getSender(MimeMessage mimeMessage) throws MessagingException {
        return ((InternetAddress)mimeMessage.getFrom()[0]).getAddress();
    }
}
