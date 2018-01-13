package test.spring.loggers;

import test.spring.model.Event;

public interface EventLogger {

    void logEvent(Event event);
}
