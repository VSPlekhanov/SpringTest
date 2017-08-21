package com.epam.lstrsum.email.service;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public interface MailService {
    static String getAddressFrom(Address[] rawAddress) {
        InternetAddress internetAddress = (InternetAddress) rawAddress[0];
        return internetAddress.getAddress();
    }

    void sendMessage(String subject, String text, String... to) throws MessagingException;

    void sendMessage(MimeMessage mimeMessage) throws MessagingException;
}
