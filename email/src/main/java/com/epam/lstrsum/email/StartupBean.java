package com.epam.lstrsum.email;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;

@Profile("email")
@ConfigurationProperties(prefix = "email")
@Service
@Slf4j
public class StartupBean {

    @Setter
    private String backupDir;

    @Setter
    private boolean ignoreInvalidSettings = false;

    @PostConstruct
    public void init() throws Exception {
        if (ignoreInvalidSettings) {
            return;
        }

        File backupDirHandler = new File(backupDir);

        log.debug("Set backup directory handler: {}", backupDir);

        if (backupDir.isEmpty()) {
            Exception e = new Exception("Error: no config value email.backup-dir is present");
            log.error(e.getMessage());
            throw e;
        } else if (!backupDirHandler.canWrite()) {
            Exception e = new Exception(String.format("Error: email.backup-dir '%s' is not writable or not exists", backupDir));
            log.error(e.getMessage());
            throw e;
        }
    }
}
