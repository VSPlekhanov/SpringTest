package com.epam.lstrsum;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;

@Profile("email")
@ConfigurationProperties(prefix = "mail")
@Service
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

        if (backupDir.isEmpty()) {
            throw new Exception("Error: no config value email.backup-dir is present");
        } else if (!backupDirHandler.canWrite()) {
            throw new Exception(String.format("Error: email.backup-dir '%s' is not writable or not exists", backupDir));
        }
    }
}
