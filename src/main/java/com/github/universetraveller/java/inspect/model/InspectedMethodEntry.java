package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.MethodEntryEvent;
import com.github.universetraveller.java.inspect.util.JDIReachingUtil;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;

public class InspectedMethodEntry extends InspectedMethod {
    private StackFrame nowFrame;
    public static InspectedMethodEntry getInstance(Inspector inspector, MethodEntryEvent event){
        InspectedMethodEntry instance = new InspectedMethodEntry();
        InspectedEvent.init(instance, inspector, event);
        instance.methodInstance = event.method();
        instance.location = new LocationSnapshot(event.location());
        ThreadReference t = null;
        boolean lock = false;
        try{
            t = event.thread();
            lock = JDIReachingUtil.suspend(t);
            instance.frameDepth = t.frameCount();
            instance.stack = inspector.getMaxFrameCountToInspect() > t.frameCount() ? t.frames() : t.frames(0, inspector.getMaxFrameCountToInspect());
            instance.caller = t.frame(1);
            instance.nowFrame = t.frame(0);
        }catch(IncompatibleThreadStateException | VMDisconnectedException e){
            instance.frameDepth = -1;
            instance.stack = null;
            instance.caller = null;
            instance.nowFrame = null;
        }finally{
            if(t != null && lock)
                JDIReachingUtil.resume(t);
        }
        instance.buildString();
        return instance;
    }
    public void register(Inspector manager){
        manager.registerMethod(this);
    }
    protected String internalBuildString(){
        StringBuffer builder = new StringBuffer();
        String callerName = "<UNKNOWN>";
        if(this.caller != null)
            callerName = String.format("%s(%s)", this.caller.location().method(), this.caller.location());
        builder.append(String.format("<MethodEntry method='%s' location='%s' frameDepth='%s' caller='%s' returnType='%s'>", this.methodInstance, this.location, this.frameDepth, callerName, this.methodInstance.returnTypeName())).append("\n");
        for(String typeName : this.methodInstance.argumentTypeNames())
            builder.append("\t").append(String.format("<Arg name='%s'/>", typeName)).append("\n");
        builder.append("</MethodEntry>");
        return builder.toString();
    }
}
