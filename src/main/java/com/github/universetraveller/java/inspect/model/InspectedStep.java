package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.event.StepEvent;

public class InspectedStep extends InspectedBreakpoint{
    public static InspectedStep getInstance(Inspector inspector, StepEvent event){
        InspectedStep instance = new InspectedStep();
        InspectedEvent.init(instance, inspector, event);
        instance.location = new LocationSnapshot(event.location());
        instance.name = "Line";
        initVariables(instance);
        inspectThread(instance, inspector, event.thread());
        instance.buildString();
        return instance;
    }
}
