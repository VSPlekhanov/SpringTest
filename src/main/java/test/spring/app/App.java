package test.spring.app;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import test.spring.loggers.EventLogger;
import test.spring.loggers.EventType;
import test.spring.model.Client;
import test.spring.loggers.Event;

import java.util.Map;

public class App {
    private Client client;
    private EventLogger defaultLogger;
    private Map<EventType, EventLogger> loggers;


    App(Client client, EventLogger defaultLogger, Map<EventType, EventLogger> loggers) {
        this.client = client;
        this.defaultLogger = defaultLogger;
        this.loggers = loggers;
    }

    public static void main(String[] args) throws Exception {
        try(ConfigurableApplicationContext context =
                    new ClassPathXmlApplicationContext("config.xml")) {
            App app = (App) context.getBean("app");
            app.logEvents(context);
        }
    }

    private void logEvents(ApplicationContext context){

        Event event = context.getBean(Event.class);
        logEvent(event.setType(EventType.INFO).setMsg("Some event for 1"));

        event = context.getBean(Event.class);
        logEvent(event.setType(EventType.INFO).setMsg("One more event for 1"));

        event = context.getBean(Event.class);
        logEvent(event.setType(EventType.INFO).setMsg("And one more event for 1"));

        event = context.getBean(Event.class);
        logEvent(event.setType(EventType.ERROR).setMsg("Some event for 2"));

        event = context.getBean(Event.class);
        logEvent(event.setMsg("Some event for 3"));
    }

    private void logEvent(Event event) {
        event.setMsg(event.getMsg().replaceAll(
                Integer.toString(client.getId()), client.getFullName()));

        EventLogger logger = loggers.get(event.getType());
        if(logger == null)
            defaultLogger.logEvent(event);
        else
            logger.logEvent(event);
    }
}
