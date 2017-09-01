package com.epam.lstrsum.service;

import javax.mail.internet.MimeMessage;

public interface MailReceiver {
    void receiveMessageAndHandleIt(final MimeMessage message) throws Exception;

    void handleMessageWithoutBackup(final MimeMessage message) throws Exception;
}
