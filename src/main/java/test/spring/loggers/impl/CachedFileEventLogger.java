package test.spring.loggers.impl;

import test.spring.model.Event;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CachedFileEventLogger extends FileEventLogger{

    private int maxCacheSize;
    private List<Event> cache = new ArrayList<>();
    private FileEventLogger fileEventLogger;

    @Override
    public void init() throws IOException {
        fileEventLogger.init();
    }

    public CachedFileEventLogger(String filename, int maxCacheSize){
        super(filename);
        fileEventLogger = new FileEventLogger(filename);
        this.maxCacheSize = maxCacheSize;
    }

    private void logEvents(){
        for (Event e : cache) {
            fileEventLogger.logEvent(e);
        }
    }

    @Override
    public void logEvent(Event event) {
        cache.add(event);
        if(cache.size() == maxCacheSize){
            logEvents();
            cache.clear();
        }
    }

    private void destroy(){
        if(!cache.isEmpty())
            logEvents();
    }
}
