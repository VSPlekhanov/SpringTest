package test.spring.loggers.impl;

import test.spring.loggers.EventLogger;
import test.spring.loggers.Event;

public class ConsoleEventLogger implements EventLogger {

    public void logEvent(Event event) {
        System.out.println(event);
    }
}
