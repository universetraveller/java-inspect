package com.github.universetraveller.java.inspect.model;

import java.io.InputStream;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.EventSet;

public abstract class BaseInspector {
    protected VirtualMachine vm;
    public VirtualMachine getVm() {
        return vm;
    }
    public void setVm(VirtualMachine vm) {
        this.vm = vm;
    }
    public abstract EventSet getVMEventSet() throws InterruptedException;
    public abstract void resume();
    protected void beforeResume(){
        // Do nothing there
        // debuggers should override it
    }
    public InputStream getVMInputStream(){
        return null;
    }

    public InputStream getVMErrorStream(){
        return null;
    }
}
