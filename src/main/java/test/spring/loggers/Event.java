package test.spring.loggers;

import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

public class Event {

    private int id = new Random().nextInt(1000);
    private String msg;
    private Date date;
    private DateFormat dateFormat;
    private EventType type;

    public Event(Date date, DateFormat dateFormat) {
        this.date = date;
        this.dateFormat = dateFormat;
    }

    @Override
    public String toString() {
        return "Event{" + "type= " + type +
                ", id= " + id +
                ", msg= " + msg +
                ", date= " + dateFormat.format(date) +
                "}\n";
    }


    public EventType getType() {
        return type;
    }

    public Event setType(EventType type) {
        this.type = type;
        return this;
    }

    public int getId() {
        return id;
    }

    public Event setId(int id) {
        this.id = id;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Event setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
