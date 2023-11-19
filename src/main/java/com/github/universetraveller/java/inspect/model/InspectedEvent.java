package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.event.Event;

public abstract class InspectedEvent {
    protected long eventTime;
    protected long id;
    protected Event eventInstance;
    protected void init(InspectedEvent instance, Inspector inspector, Event event){
        instance.eventTime = inspector.getRunningTime();
        instance.id = inspector.getNextId();
        instance.eventInstance = event;
    }
    public abstract String buildString();
    public String toString(){
        return buildString();
    }
}
