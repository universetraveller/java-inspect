package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.event.Event;

public abstract class InspectorRunner {
    public abstract void run(Inspector inspectorInstance) throws Exception;
    public abstract void handleEvent(Inspector inspector, Event event);
}
