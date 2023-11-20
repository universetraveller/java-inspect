package com.github.universetraveller.java.inspect.model;

import java.util.ArrayList;
import java.util.List;

import com.github.universetraveller.java.inspect.util.JDIReachingUtil;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.event.ExceptionEvent;

public class InspectedException extends InspectedEvent {
   ObjectReference exceptionReference;
   Location location;
   List<String> stackTrace;
   public static InspectedException getInstance(Inspector inspector, ExceptionEvent event){
        InspectedException instance = new InspectedException();
        InspectedEvent.init(instance, inspector, event);
        instance.location = event.catchLocation();
        instance.exceptionReference = event.exception();
        instance.stackTrace = new ArrayList<>();
        try{
            event.thread().suspend();
            int depth = inspector.getMaxFrameCountToInspect() > event.thread().frameCount() ? event.thread().frameCount() : inspector.getMaxFrameCountToInspect();
            instance.stackTrace = JDIReachingUtil.buildStackTrace(event.thread().frames(0, depth));
        }catch(AbsentInformationException e){
            inspector.getLogger().warning(String.format("%s occurs at building stacktrace at handling exception event; skip", e));
        }catch(IncompatibleThreadStateException f){
            inspector.getLogger().warning(String.format("%s occurs at building stacktrace at handling exception event; skip", f));
        }finally{
            event.thread().resume();
        }
        return instance;
   } 
   public String buildString(){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("<Exception type='%s' occurAt='%s'>", this.exceptionReference, this.location)).append("\n");
        for(String line : this.stackTrace)
            builder.append("\t").append(String.format("<StackTrace value='%s'/>", line)).append("\n");
        builder.append("</Exception>");
        return builder.toString();
   }
}
