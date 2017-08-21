package com.epam.lstrsum.email.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;

public interface BackupHelper {
    void backupEmail(MimeMessage mimeMessage) throws IOException, MessagingException;

    List<String> findAllBackup();

    MimeMessage getMessageByFilename(String filename) throws IOException, MessagingException;

}