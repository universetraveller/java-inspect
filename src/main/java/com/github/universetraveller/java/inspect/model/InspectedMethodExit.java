package com.github.universetraveller.java.inspect.model;

import com.github.universetraveller.java.inspect.util.JDIReachingUtil;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodExitEvent;

public class InspectedMethodExit extends InspectedMethod{
    private long exectionTime; 
    public long getExectionTime() {
        return exectionTime;
    }
    private Value exectionValue;
    public Value getExectionValue() {
        return exectionValue;
    }
    private StackFrame destFrame;
    private String exitCause;
    public String getExitCause() {
        return exitCause;
    }
    public static InspectedMethodExit getInstance(Inspector inspector, MethodExitEvent event){
        InspectedMethodExit instance = new InspectedMethodExit();
        InspectedEvent.init(instance, inspector, event);
        instance.methodInstance = event.method();
        instance.location = new LocationSnapshot(event.location());
        instance.exectionValue = event.returnValue();
        ThreadReference t = null;
        boolean lock = false;
        try{
            t = event.thread();
            lock = JDIReachingUtil.suspend(t);
            instance.frameDepth = t.frameCount();
            instance.stack = inspector.getMaxFrameCountToInspect() > t.frameCount() ? t.frames() : t.frames(0, inspector.getMaxFrameCountToInspect());        
            instance.caller = t.frameCount() > 0 ? t.frame(0) : null;
            instance.destFrame = instance.caller;
            instance.callerLocation = instance.caller == null ? null : new LocationSnapshot(instance.caller.location());
        }catch(IncompatibleThreadStateException | VMDisconnectedException e){
            instance.frameDepth = -1;
            instance.stack = null;
            instance.caller = null;
            instance.destFrame = null;
        }
        instance.exectionTime = -1;
        instance.exitCause = "return";
        instance.buildString();
        if(t != null && lock)
            JDIReachingUtil.resume(t);
        return instance;
    }
    public static InspectedMethodExit getInstance(Inspector inspector, ExceptionEvent event){
        InspectedMethodExit instance = new InspectedMethodExit();
        InspectedEvent.init(instance, inspector, event);
        instance.methodInstance = event.catchLocation().method();
        instance.location = new LocationSnapshot(event.catchLocation());
        instance.exectionValue = null;
        ThreadReference t = null;
        boolean lock = false;
        try{
            t = event.thread();
            lock = JDIReachingUtil.suspend(t);
            instance.frameDepth = t.frameCount();
            instance.stack = inspector.getMaxFrameCountToInspect() > t.frameCount() ? t.frames() : t.frames(0, inspector.getMaxFrameCountToInspect());        
            instance.caller = t.frameCount() > 0 ? t.frame(0) : null;
            instance.destFrame = instance.caller;
            instance.callerLocation = instance.caller == null ? null : new LocationSnapshot(instance.caller.location());
        }catch(IncompatibleThreadStateException | VMDisconnectedException e){
            instance.frameDepth = -1;
            instance.stack = null;
            instance.caller = null;
            instance.destFrame = null;
        }
        instance.exectionTime = -1;
        instance.exitCause = "exception";
        instance.buildString();
        if(t != null && lock)
            JDIReachingUtil.resume(t);
        return instance;
    }
    public void finish(Inspector inspector){
        InspectedMethodEntry entry = inspector.finishMethod(this);
        if(entry != null)
            this.exectionTime = this.eventTime - entry.getEventTime();
        this.buildString();
    }
    protected String internalBuildString(){
        String callerName = "<UNKNOWN>";
        if(this.callerLocation != null)
            callerName = String.format("%s(%s)", this.callerLocation.method(), this.callerLocation);
        return String.format("<MethodExit method='%s' location='%s' frameDepth='%s' returnValue='%s' executionTime='%s' caller='%s' exitCause='%s'/>", this.methodInstance, this.location, this.frameDepth, this.exectionValue, this.exectionTime, callerName, this.exitCause);
    }
}
