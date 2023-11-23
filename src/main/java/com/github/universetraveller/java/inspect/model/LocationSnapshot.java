package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.tools.jdi.ReferenceTypeImpl;

public class LocationSnapshot implements Location {
    private Location location;
    private Method methodCache;
    private long codeIndexCache;
    private ReferenceType referenceTypeCache;
    private int lineNumberCache;
    private String sourceNameCache;
    private String sourcePathCache;
    private VirtualMachine virtualMachineCache;
    private String toStringCache;
    public LocationSnapshot(Location location){
        this.location = location;
        this.methodCache = location.method();
        this.codeIndexCache = location.codeIndex();
        this.referenceTypeCache = location.declaringType();
        this.lineNumberCache = location.lineNumber();
        try{
            this.sourceNameCache = location.sourceName();
            this.sourcePathCache = location.sourcePath();
        }catch(AbsentInformationException e){
            this.sourceNameCache = "<UNKNOWN>";
            this.sourcePathCache = "<UNKNOWN>";
        }
        this.virtualMachineCache = location.virtualMachine();
        this.toStringCache = location.toString();
    }

    @Override
    public String toString() {
        try{
            return location.toString();
        }catch(Exception e){
            return this.toStringCache;
        }
    }

    @Override
    public long codeIndex() {
        try{
            return location.codeIndex();
        }catch(Exception e){
            return this.codeIndexCache;
        }
    }

    @Override
    public ReferenceType declaringType() {
        try{
            return location.declaringType();
        }catch(Exception e){
            return this.referenceTypeCache;
        }
    }

    @Override
    public int lineNumber() {
        try{
            return location.lineNumber();
        }catch(Exception e){
            return this.lineNumberCache;
        }
    }

    @Override
    public int lineNumber(String arg0) {
        return location.lineNumber(arg0);
    }

    @Override
    public Method method() {
        try{
            return location.method();
        }catch(Exception e){
            return this.methodCache;
        }
    }

    @Override
    public String sourceName() throws AbsentInformationException {
        try{
            return location.sourceName();
        }catch(AbsentInformationException e){
            throw e;
        }catch(Exception e){
            return this.sourceNameCache;
        }
    }

    @Override
    public String sourceName(String arg0) throws AbsentInformationException {
        return location.sourceName(arg0);
    }

    @Override
    public String sourcePath() throws AbsentInformationException {
        try{
            return location.sourcePath();
        }catch(AbsentInformationException e){
            throw e;
        }catch(Exception e){
            return this.sourcePathCache;
        }
    }

    @Override
    public String sourcePath(String arg0) throws AbsentInformationException {
        return location.sourcePath(arg0);
    }

    @Override
    public VirtualMachine virtualMachine() {
        try{
            return location.virtualMachine();
        }catch(Exception e){
            return this.virtualMachineCache;
        }
    }

    @Override
    public int compareTo(Location o) {
        return location.compareTo(o);
    }

}
