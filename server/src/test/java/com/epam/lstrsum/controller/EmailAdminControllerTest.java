package com.epam.lstrsum.controller;

import com.epam.lstrsum.email.service.BackupHelper;
import com.epam.lstrsum.service.MailReceiver;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.mail.internet.MimeMessage;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EmailAdminControllerTest {
    private BackupHelper backupHelper = mock(BackupHelper.class);
    private MailReceiver mailReceiver = mock(MailReceiver.class);

    private EmailAdminController emailAdminController = new EmailAdminController(
            backupHelper, mailReceiver
    );

    @Test
    public void getAllEmailBackupFileNames() throws Exception {
        List<String> expected = singletonList("someSting");
        doReturn(expected).when(backupHelper).findAllBackup();

        assertThat(emailAdminController.getAllEmailBackupFileNames())
                .isEqualToComparingFieldByFieldRecursively(ResponseEntity.ok(expected));

        verify(backupHelper, times(1)).findAllBackup();
    }

    @Test
    public void reprocess() throws Exception {
        String someFileName = "someFileName";
        MimeMessage mock = mock(MimeMessage.class);
        doReturn(mock).when(backupHelper).getMessageByFilename(eq(someFileName));

        assertThat(emailAdminController.reprocess(someFileName).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        verify(backupHelper, times(1)).getMessageByFilename(eq(someFileName));
        verify(mailReceiver, times(1)).handleMessageWithoutBackup(eq(mock));
    }

}