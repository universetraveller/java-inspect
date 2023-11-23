package com.github.universetraveller.java.inspect.model;

import java.util.List;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

public class StackFrameSnapshot implements StackFrame {

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

    @Override
    public List<Value> getArgumentValues() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Value getValue(LocalVariable arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<LocalVariable, Value> getValues(List<? extends LocalVariable> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Location location() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setValue(LocalVariable arg0, Value arg1) throws InvalidTypeException, ClassNotLoadedException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ObjectReference thisObject() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ThreadReference thread() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LocalVariable visibleVariableByName(String arg0) throws AbsentInformationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<LocalVariable> visibleVariables() throws AbsentInformationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VirtualMachine virtualMachine() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
