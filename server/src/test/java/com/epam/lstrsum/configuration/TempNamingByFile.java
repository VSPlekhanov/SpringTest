package com.epam.lstrsum.configuration;

import de.flapdoodle.embed.process.extract.ITempNaming;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TempNamingByFile implements ITempNaming {
    public static final String TEMP_FILE_NAME = "file";
    private final File informationFile;

    @Override
    public String nameFor(String prefix, String postfix) {
        String username = System.getProperty("user.name");

        if (informationFile.exists()) {
            try {
                String fileContent = FileUtils.readFileToString(informationFile, "UTF-8");
                int value = Integer.parseInt(fileContent) + 1;

                FileWriter fooWriter = new FileWriter(informationFile, false);
                fooWriter.append(String.valueOf(value));
                fooWriter.close();

                return prefix + "-" + username + "-" + postfix + "-" + value;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException(new FileNotFoundException("Can't find temp file"));
        }
    }
}
