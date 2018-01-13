package test.spring.loggers.impl;

import org.apache.commons.io.FileUtils;
import test.spring.loggers.EventLogger;
import test.spring.model.Event;

import java.io.File;
import java.io.IOException;

public class FileEventLogger implements EventLogger {

    private String fileName;
    private File file;

    public void init()throws IOException{
        this.file = new File(fileName);
        if (!file.canWrite()) throw new IOException("Can't write to file");
    }

    public FileEventLogger(String filename) {
        this.fileName = filename;
    }

    @Override
    public void logEvent(Event event) {
        try {
            FileUtils.writeStringToFile(file, event.toString(), "UTF-8", true);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
