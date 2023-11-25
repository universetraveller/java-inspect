package com.github.universetraveller.java.inspect.inspector;
import java.io.IOException;

import com.github.universetraveller.java.inspect.model.InspectedBreakpoint;
import com.github.universetraveller.java.inspect.model.InspectedClassPrepare;
import com.github.universetraveller.java.inspect.model.InspectedException;
import com.github.universetraveller.java.inspect.model.InspectedFieldAccess;
import com.github.universetraveller.java.inspect.model.InspectedFieldModification;
import com.github.universetraveller.java.inspect.model.InspectedMethodEntry;
import com.github.universetraveller.java.inspect.model.InspectedMethodExit;
import com.github.universetraveller.java.inspect.model.InspectedMethodInvoking;
import com.github.universetraveller.java.inspect.model.InspectedOutput;
import com.github.universetraveller.java.inspect.model.InspectedStep;
import com.github.universetraveller.java.inspect.model.Inspector;
import com.github.universetraveller.java.inspect.model.InspectorRunner;
import com.github.universetraveller.java.inspect.model.LocationSnapshot;
import com.github.universetraveller.java.inspect.util.JDIReachingUtil;
import com.github.universetraveller.java.inspect.util.StreamUtil;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
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
public class DefaultInspectorRunner extends InspectorRunner {
    public void run(Inspector instance) throws Exception {
        EventSet events = null;
        while((events = instance.getVMEventSet()) != null){
            instance.checkTimeout();
            for(Event event : events){
                    if(event instanceof VMDisconnectEvent){
                        instance.getLogger().info("VM is going to disconect");
                        return;
                    }
                    handleOutput(instance);
                    handleEvent(instance, event);
                    instance.resume();
            }
        }
    }

    public void handleEvent(Inspector instance, Event event) {
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
            buildInvokingTail(instance, event);
            instance.removeHandlingMethod(((MethodExitEvent)event).method());
            instance.enableGlobalEntryHandler();
        }
    }

    private static void buildMethodExit(Inspector instance, Event event) {
        InspectedMethodExit methodExit = InspectedMethodExit.getInstance(instance, (MethodExitEvent)event);
        instance.addEvent(methodExit);
        methodExit.finish(instance);
        instance.getLogger().fine("Exit " + methodExit.getMethodInstance());
    }

    private static void buildInvokingTail(Inspector instance, Event event) {
        InspectedMethodInvoking invocation = instance.getGlobalInvoking();
	if(invocation == null){
		instance.getLogger().warning(String.format("Try to finish null when handling %s; skip it", event));
		return;
	}
	invocation.finish(event);
        instance.getLogger().fine("End invocation of " + instance.getGlobalInvoking().getTail().getMethodInstance());
        instance.setGlobalInvoking(null);
    }

    private static void buildInvokingHead(Inspector instance, Event event) {
        InspectedMethodInvoking invocation = InspectedMethodInvoking.getInstance(instance);
        instance.setGlobalInvoking(invocation);
        invocation.register((MethodEntryEvent)event);
        instance.addEvent(invocation);
        instance.getLogger().fine("Start invoking " + invocation.getHead().getMethodInstance());
    }

    private static void buildMethodEntry(Inspector instance, Event event) {
        InspectedMethodEntry entry = InspectedMethodEntry.getInstance(instance, (MethodEntryEvent)event);
        instance.addEvent(entry);
        entry.register(instance);
        instance.getLogger().fine("Enter " + entry.getMethodInstance());
    }

    private static void handleStepEvent(Inspector instance, Event event) {
        instance.addEvent(InspectedStep.getInstance(instance, (StepEvent)event));
        instance.getLogger().info(event.toString());
    }

    private static void handleModificationWatchpointEvent(Inspector instance, Event event) {
        instance.addEvent(InspectedFieldModification.getInstance(instance, ((ModificationWatchpointEvent)event)));
        instance.getLogger().fine("Modify " + ((ModificationWatchpointEvent)event).field());
    }

    private static void handleAccessWatchpointEvent(Inspector instance, Event event) {
        instance.addEvent(InspectedFieldAccess.getInstance(instance, (AccessWatchpointEvent)event));
        instance.getLogger().fine("Access " + ((AccessWatchpointEvent)event).field());
    }

    private static void handleExceptionEvent(Inspector instance, Event event) {
        instance.getLogger().fine(String.format("Handle exception <%s>", event));
        ExceptionEvent exceptionEvent = (ExceptionEvent)event;
        if(!instance.isInspectImplicitMethod() && !instance.canHandleMethodEntry()){
            try{
                Location loc = exceptionEvent.thread().frame(0).location();
                if(instance.isHandlingGlobalMethod(loc.method())){
                    buildInvokingTail(instance, exceptionEvent);
                    instance.removeHandlingMethod(loc.method());
                    instance.enableGlobalEntryHandler();
                    return;
                }
            }catch(IncompatibleThreadStateException e){
                instance.getLogger().warning(String.format("Cannot get frame while handling %s so cannot detect if it is handling global method", e));
            }
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
        StringBuffer builder = new StringBuffer();
        String output = "";
        try{
            StreamUtil.readFromStream(builder, instance.getVMInputStream());
            output = builder.toString();
	    if(!output.isEmpty()){
		    instance.getLogger().info("Normal Output: " + output);
		    instance.addEvent(InspectedOutput.getInstance(instance, output, InspectedOutput.STDOUT));
	    }
        }catch(IOException e){
            instance.getLogger().warning("Failed to read input stream");
            e.printStackTrace();
        }
        builder = new StringBuffer();
        try{
            StreamUtil.readFromStream(builder, instance.getVMErrorStream());
            output = builder.toString();
	    if(!output.isEmpty()){
		    instance.getLogger().info("Error Output: " + output);
		    instance.addEvent(InspectedOutput.getInstance(instance, output, InspectedOutput.STDERR));
	    }
        }catch(IOException e){
            instance.getLogger().warning("Failed to read input stream");
            e.printStackTrace();
        }
    }
}
