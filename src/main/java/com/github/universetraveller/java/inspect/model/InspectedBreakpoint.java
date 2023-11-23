package com.github.universetraveller.java.inspect.model;

import java.util.List;


import com.github.universetraveller.java.inspect.util.JDIReachingUtil;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.BreakpointEvent;

public class InspectedBreakpoint extends InspectedEvent {
    protected Location location;
    private int frameDepth;
    protected List<InspectedVariable> variables;
    protected List<InspectedVariableChange> variableChanges;
    protected String name;
    protected static void initVariables(InspectedBreakpoint instance){
        instance.frameDepth = -1;
        instance.variables = null;
        instance.variableChanges = null;
    }
    protected static void inspectThread(InspectedBreakpoint instance, Inspector inspector, ThreadReference t){
        boolean lock = JDIReachingUtil.suspend(t);
        try{
            List<StackFrame> targetFrames = inspector.getMaxFrameCountToInspect() > t.frameCount() ? t.frames() : t.frames(0, inspector.getMaxFrameCountToInspect());
            if(inspector.isInspectVariables())
                instance.variables = JDIReachingUtil.inspectVariables(targetFrames);
            if(inspector.isInspectVariableChanges())
                instance.variableChanges = inspector.updateVariableMap(targetFrames);
            instance.frameDepth = t.frameCount();
        }catch(AbsentInformationException e){
            inspector.getLogger().warning(String.format("%s occurs at inspecting variables at handling breakpoint event; skip", e));
        }catch(IncompatibleThreadStateException | VMDisconnectedException f){
            inspector.getLogger().warning(String.format("%s occurs at inspecting variables at handling breakpoint event; skip", f));
        }finally{
            if(lock)
                JDIReachingUtil.resume(t);
        }
    }
    public static InspectedBreakpoint getInstance(Inspector inspector, BreakpointEvent event){
        InspectedBreakpoint instance = new InspectedBreakpoint();
        InspectedEvent.init(instance, inspector, event);
        instance.name = "Breakpoint";
        instance.location = new LocationSnapshot(event.location());
        initVariables(instance);
        inspectThread(instance, inspector, event.thread());
        instance.buildString();
        return instance;
    }

    protected String internalBuildString(){
        StringBuffer builder = new StringBuffer();
        builder.append(String.format("<%s location='%s' frameDepth='%s'", this.name, this.location, this.frameDepth));
        boolean addVariables = false;
        if(this.variables != null && !this.variables.isEmpty())
            addVariables = true;
        boolean addVariableChanges = false;
        if(this.variableChanges != null && !this.variableChanges.isEmpty())
            addVariableChanges = true;
        if(!addVariables && !addVariableChanges){
            builder.append("/>");
            return builder.toString();
        }
        builder.append(">\n");
        if(addVariables)
            for(InspectedVariable var : this.variables)
                builder.append("\t").append(var).append("\n");
        if(addVariableChanges)
            for(InspectedVariableChange change : variableChanges)
                builder.append("\t").append("<VariableChange ").append(change.toString()).append("/>\n");
        builder.append(String.format("</%s>", this.name));
        return builder.toString();
    }
    
}
