package com.github.universetraveller.java.inspect.inspector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.github.universetraveller.java.inspect.model.InspectedBreakpoint;
import com.github.universetraveller.java.inspect.model.InspectedClassPrepare;
import com.github.universetraveller.java.inspect.model.InspectedEvent;
import com.github.universetraveller.java.inspect.model.InspectedException;
import com.github.universetraveller.java.inspect.model.Inspector;
import com.github.universetraveller.java.inspect.model.InspectorRunner;
import com.github.universetraveller.java.inspect.util.StreamUtil;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.tools.classfile.Annotation.element_value;
public class DefaultInspectorRunner extends InspectorRunner {
    public void run(Inspector instance) throws Exception {
        EventSet events = null;
        while((events = instance.getVMEventSet()) != null){
            for(Event event : events){
                    if(event instanceof VMDisconnectEvent){
                        instance.getLogger().info("VM is going to disconect");
                        return;
                    }
                    handleEvent(instance, event);
                    handleOutput(instance);
                    instance.beforeResume();
                    instance.resume();
            }
        }
    }

    private static void handleEvent(Inspector instance, Event event) {
        if(event instanceof ClassPrepareEvent)
            handleClassPrepareEvent(instance, event);
        if(event instanceof BreakpointEvent)
            handleBreakPointEvent(instance, event);
        if(event instanceof MethodEntryEvent)
            handleMethodEntryEvent(instance, event);
        if(event instanceof MethodExitEvent)
            handleMethodExitEvent(instance, event);
        if(event instanceof ExceptionEvent)
            handleExceptionEvent(instance, event);
        if(event instanceof AccessWatchpointEvent)
            handleAccessWatchpointEvent(instance, event);
        if(event instanceof ModificationWatchpointEvent)
            handleModificationWatchpointEvent(instance, event);
        if(event instanceof StepEvent)
            handleStepEvent(instance, event);
    }

    private static void handleMethodExitEvent(Inspector instance, Event event) {
        boolean isGlobal = instance.isGlobalMethodExitRequest(event.request());
        if(!isGlobal || instance.isInspectImplicitMethod()){
            buildMethodExit(instance, event);
        } else if(instance.isHandlingGlobalMethod(((MethodExitEvent)event).method())){
            instance.removeHandlingMethod(((MethodExitEvent)event).method());
            instance.enableGlobalEntryHandler();
            buildInvokingTail(instance, event);
        }
    }

    private static void buildMethodExit(Inspector instance, Event event) {
        // TODO 
    }

    private static void buildInvokingTail(Inspector instance, Event event) {
        // TODO
    }

    private static void handleStepEvent(Inspector instance, Event event) {
       // TODO 
    }

    private static void handleModificationWatchpointEvent(Inspector instance, Event event) {
        // TODO
    }

    private static void handleAccessWatchpointEvent(Inspector instance, Event event) {
        // TODO add access
    }

    private static void handleExceptionEvent(Inspector instance, Event event) {
        instance.getLogger().fine(String.format("Handle exception <%s>", event));
        ExceptionEvent exceptionEvent = (ExceptionEvent)event;
        if(!instance.isInspectImplicitMethod() && !instance.canHandleMethodEntry() && instance.isHandlingGlobalMethod(exceptionEvent.catchLocation().method())){
            buildInvokingTail(instance, exceptionEvent);
            return;
        }
        instance.addEvent(InspectedException.getInstance(instance, exceptionEvent));
    }

    private static void handleMethodEntryEvent(Inspector instance, Event event) {
        boolean isGlobal = instance.isGlobalMethodEntryRequest(event.request());
        if(!isGlobal || instance.isInspectImplicitMethod()){
            buildMethodEntry(instance, event);
        } else if(instance.canHandleMethodEntry()){
            buildInvokingHead(instance, event);
            instance.disableGlobalEntryHandler();
            instance.addHandlingMethod(((MethodEntryEvent)event).method());
        }
    }

    private static void buildInvokingHead(Inspector instance, Event event) {
        // TODO build invoking head
    }

    private static void buildMethodEntry(Inspector instance, Event event) {
        // TODO build entry
    }

    private static void handleBreakPointEvent(Inspector instance, Event event) {
        if(instance.isOneShotBreakPoint())
            event.request().disable();
        instance.addEvent(InspectedBreakpoint.getInstance(instance, (BreakpointEvent)event));
        instance.getLogger().fine(String.format("Handle breakpoint <%s>", event));
        instance.makeMethodEntryRequest((BreakpointEvent)event);
        instance.makeStepRequest((BreakpointEvent)event);
        instance.makeExceptionRequest((BreakpointEvent)event);
    }

    private static void handleClassPrepareEvent(Inspector instance, Event event) {
        instance.getLogger().fine(String.format("Handle class preparing <%s>", event));
        instance.addEvent(InspectedClassPrepare.getInstance(instance, (ClassPrepareEvent)event));
        instance.tryToInspectClass((ClassPrepareEvent)event);
    }

    private static void handleOutput(Inspector instance){
        if(!instance.isInspectOutput())
            return;
        StringBuilder builder = new StringBuilder();
        String output = "";
        try{
            StreamUtil.readFromStream(builder, instance.getVMInputStream());
            output = builder.toString();
            instance.getLogger().info("Normal Output: " + output);
            // TODO add output to instance's events
        }catch(IOException e){
            instance.getLogger().warning("Failed to read input stream");
        }
        builder = new StringBuilder();
        try{
            StreamUtil.readFromStream(builder, instance.getVMErrorStream());
            output = builder.toString();
            instance.getLogger().info("Error Output: " + output);
            // TODO add output to instance's events
        }catch(IOException e){
            instance.getLogger().warning("Failed to read input stream");
        }
    }
}
