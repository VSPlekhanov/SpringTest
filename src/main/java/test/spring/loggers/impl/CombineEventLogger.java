package test.spring.loggers.impl;

import test.spring.loggers.Event;
import test.spring.loggers.EventLogger;

import java.util.List;

public class CombineEventLogger implements EventLogger {

    private List<EventLogger> loggers;

    CombineEventLogger(List<EventLogger> loggers){
        this.loggers = loggers;
    }

    @Override
    public void logEvent(Event event) {
        for (EventLogger logger: loggers) {
            logger.logEvent(event);
        }
    }
}
