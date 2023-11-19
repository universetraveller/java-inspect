package com.github.universetraveller.java.inspect.model;

import java.util.ArrayList;
import java.util.List;

import com.github.universetraveller.java.inspect.util.JDIReachingUtil;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.BreakpointEvent;

public class InspectedBreakpoint extends InspectedEvent {
    private Location location;
    private List<InspectedVariable> variables;
    public static InspectedBreakpoint getInstance(Inspector inspector, BreakpointEvent event){
        InspectedBreakpoint instance = new InspectedBreakpoint();
        instance.init(instance, inspector, event);
        instance.variables = new ArrayList<>();
        try{
            ThreadReference t = event.thread();
            instance.location = event.location();
            int depth = inspector.getMaxFrameCountToInspect() > t.frameCount() ? t.frameCount() : inspector.getMaxFrameCountToInspect();
            instance.variables = JDIReachingUtil.inspectVariables(t.frames(0, depth));
        }catch(AbsentInformationException e){
            inspector.getLogger().warning(String.format("%s occurs at inspecting variables at handling breakpoint event; skip", e));
        }catch(IncompatibleThreadStateException f){
            inspector.getLogger().warning(String.format("%s occurs at inspecting variables at handling breakpoint event; skip", f));
        }
        return instance;
    }
    public String buildString(){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("<Breakpoint location=%s>", this.location)).append("\n");
        for(InspectedVariable variable : this.variables){
            builder.append("\t").append(variable.toString()).append("\n");
        }
        builder.append("</Breakpoint>");
        return builder.toString();
    }
    
}
