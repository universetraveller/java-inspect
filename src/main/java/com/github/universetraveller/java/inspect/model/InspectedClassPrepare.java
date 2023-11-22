package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.ClassType;
import com.sun.jdi.event.ClassPrepareEvent;

public class InspectedClassPrepare extends InspectedEvent {
    private String className;
    private ClassType classReference;
    public static InspectedClassPrepare getInstance(Inspector inspector, ClassPrepareEvent event){
        InspectedClassPrepare instance = new InspectedClassPrepare();
        InspectedEvent.init(instance, inspector, event);
	try{
		instance.classReference = (ClassType) event.referenceType();
	}catch(ClassCastException e){
		inspector.getLogger().warning(String.format("%s occurs when initializing InspectedClassPrepare; skip", e));
		instance.classReference = null;
	}
        instance.eventInstance = event;
        return instance;
    } 
    public String buildString(){
        return String.format("<ClassPrepare className='%s' classRef='%s' time='%s' id='%s'/>", this.className, this.classReference, this.eventTime, this.id);
    }
}
