package test.spring.loggers.impl;

import org.apache.commons.io.FileUtils;
import test.spring.loggers.EventLogger;
import test.spring.model.Event;

import java.io.File;

public class FileEventLogger implements EventLogger {

    private String filename;

    public FileEventLogger(String filename) {
        this.filename = filename;
    }

    @Override
    public void logEvent(Event event) {
        try {
            FileUtils.writeStringToFile(new File(filename), event.toString(), "UTF-8", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
