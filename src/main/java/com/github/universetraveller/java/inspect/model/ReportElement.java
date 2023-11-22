package com.github.universetraveller.java.inspect.model;

public abstract class ReportElement {
    private Object instance;
    private long id;
    public ReportElement(Object instance, long id) {
        this.instance = instance;
        this.id = id;
    }
    public abstract String toString();
}
