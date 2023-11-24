package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.Field;
import com.sun.jdi.event.ModificationWatchpointEvent;

public class InspectedFieldModification  extends InspectedEvent implements InspectedField {
    private Field fieldInstance;
    private String fieldName;
    private InspectedVariableChange fieldChange;

    public Field getFieldInstance() {
        return fieldInstance;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static InspectedFieldModification getInstance(Inspector inspector, ModificationWatchpointEvent event){
        InspectedFieldModification instance = new InspectedFieldModification();
        InspectedEvent.init(instance, inspector, event);
        instance.fieldInstance = event.field();
        instance.fieldName = event.field().name();
        instance.fieldChange = new InspectedVariableChange(new InspectedVariable(instance.fieldInstance, event.valueCurrent()), new InspectedVariable(instance.fieldInstance, event.valueToBe()), event.location());
        instance.buildString();
        return instance;
    }

    protected String internalBuildString(){
        return String.format("<FieldModification %s/>", this.fieldChange.toString());
    }
}
