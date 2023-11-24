package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.Field;

public interface InspectedField {
    public Field getFieldInstance();
    public String getFieldName();
}
