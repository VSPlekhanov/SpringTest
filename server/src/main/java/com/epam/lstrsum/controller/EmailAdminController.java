package com.epam.lstrsum.controller;

import com.epam.lstrsum.email.service.BackupHelper;
import com.epam.lstrsum.service.mail.MailReceiver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.internet.MimeMessage;
import java.util.List;

@RestController
@RequestMapping("/admin/backup/email")
@RequiredArgsConstructor
@Profile("email")
public class EmailAdminController {
    private final BackupHelper backupHelper;
    private final MailReceiver mailReceiver;

    @GetMapping("/list")
    public ResponseEntity<List<String>> getAllEmailBackupFileNames() {
        return ResponseEntity.ok(backupHelper.findAllBackup());
    }

    @PostMapping("/reprocess")
    public ResponseEntity reprocess(
            @RequestBody String fileName
    ) throws Exception {
        MimeMessage messageByFilename = backupHelper.getMessageByFilename(fileName);
        mailReceiver.handleMessageWithoutBackup(messageByFilename);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
