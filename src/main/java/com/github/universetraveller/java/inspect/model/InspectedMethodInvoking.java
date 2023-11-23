package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.Method;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;

public class InspectedMethodInvoking extends InspectedEvent{
    private InspectedMethodEntry head;
    public InspectedMethodEntry getHead() {
        return head;
    }

    private InspectedMethodExit tail;
    public InspectedMethodExit getTail() {
        return tail;
    }

    private Inspector manager;
    public static InspectedMethodInvoking getInstance(Inspector inspector){
        InspectedMethodInvoking instance = new InspectedMethodInvoking();
        InspectedEvent.init(instance, inspector, null);
        instance.manager = inspector;
        instance.head = null;
        instance.tail = null;
        return instance;
    }

    public void register(MethodEntryEvent event){
        this.head = InspectedMethodEntry.getInstance(manager, event);
        this.head.register(manager);
    }

    public void finish(Event event){
        this.tail = event instanceof MethodExitEvent ? InspectedMethodExit.getInstance(manager, (MethodExitEvent)event) : InspectedMethodExit.getInstance(manager, (ExceptionEvent)event);
        this.tail.finish(manager);
        this.buildString();
    }

    protected String internalBuildString(){
        Method met = this.head.getMethodInstance();
        return String.format("<MethodInvoking method='%s' location='%s' args='%s' returnType='%s' returnValue='%s' executionTime='%s' frameDepth='%s'/>", met, met.location(), String.join(",", met.argumentTypeNames()), met.returnTypeName(), this.tail.getExectionValue(), this.tail.getExectionTime(), this.head.getFrameDepth());
    }
}
