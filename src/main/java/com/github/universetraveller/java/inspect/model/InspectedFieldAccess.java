package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.Value;
import com.sun.jdi.event.AccessWatchpointEvent;

public class InspectedFieldAccess extends InspectedEvent{
    private Value value;
    private Location location;
    private Field fieldInstance;
    private String fieldName;    
    public static InspectedFieldAccess getInstance(Inspector inspector, AccessWatchpointEvent event){
        InspectedFieldAccess instance = new InspectedFieldAccess();
        InspectedEvent.init(instance, inspector, event);
        instance.value = event.valueCurrent();
        instance.fieldInstance = event.field();
        instance.fieldName = event.field().name();
        instance.location = event.location();
        return instance;
    }

    public String buildString(){
        return String.format("<AccessField name='%s' value='%s' location='%s'/>", this.fieldInstance, this.value, this.location);
    }
}
