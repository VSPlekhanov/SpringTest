package test.spring.app;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import test.spring.loggers.EventLogger;
import test.spring.model.Client;
import test.spring.model.Event;

public class App {
    private Client client;
    private EventLogger eventLogger;


    App(Client client, EventLogger eventLogger) {
        this.client = client;
        this.eventLogger = eventLogger;
    }

    public static void main(String[] args) throws Exception {
        try(ConfigurableApplicationContext context =
                    new ClassPathXmlApplicationContext("Spring_Config.xml")) {

            App app = (App) context.getBean("app");
            Event event1 = (Event) context.getBean("event");
            event1.setMsg("User 1 is ready");
            app.logEvent(event1);
            Thread.sleep(1000);
            Event event2 = (Event) context.getBean("event");
            event2.setMsg("User 2 is ready");
            app.logEvent(event2);
            Event event3 = (Event) context.getBean("event");
            event3.setMsg("User 3 is ready");
            app.logEvent(event3);
        }
    }

    private void logEvent(Event event) {
        event.setMsg(event.getMsg().replaceAll(
                Integer.toString(client.getId()), client.getFullName()));
        eventLogger.logEvent(event);
    }
}
