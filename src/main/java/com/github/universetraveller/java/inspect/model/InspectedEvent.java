package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.event.Event;

public abstract class InspectedEvent {
    protected long eventTime;
    public long getEventTime() {
        return eventTime;
    }
    protected long id;
    public long getId() {
        return id;
    }
    protected Event eventInstance;
    protected static void init(InspectedEvent instance, Inspector inspector, Event event){
        instance.eventTime = inspector.getRunningTime();
        instance.id = inspector.getNextId();
        instance.eventInstance = event;
    }
    public abstract String buildString();
    public String toString(){
        return buildString();
    }
}
