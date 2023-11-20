package com.github.universetraveller.java.inspect.model;

import java.util.List;

import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.StackFrame;

public class InspectedMethod extends InspectedEvent {
    protected Method methodInstance;
    public Method getMethodInstance() {
        return methodInstance;
    }
    protected int frameDepth;
    public int getFrameDepth() {
        return frameDepth;
    }
    protected List<StackFrame> stack;
    protected StackFrame caller;
    public Location getLocation() {
        return location;
    }
    protected Location location;
    public String buildString(){
        return String.format("%s(%s)", methodInstance, location);
    }
}
