package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.Location;

public class InspectedVariableChange {
    private String typeName;
    private String name;
    private String fromValue;
    private String toValue;
    private InspectedVariable fromInstance;
    private InspectedVariable toInstance;
    private Location location;

    public InspectedVariableChange(InspectedVariable from, InspectedVariable to, Location loc){
        this.typeName = from.getValueType();
        if(!to.getValueType().equals(this.typeName))
            this.typeName += String.format("(To %s)", to.getValueType());
        this.name = from.getIdentifier();
        if(!to.getIdentifier().equals(this.name))
            this.name += String.format("(To %s)", to.getIdentifier());
        this.fromInstance = from;
        this.toInstance = to;
        this.fromValue = from.getValueString();
        this.toValue = to.getValueString();
        this.location = loc;
    }

    public InspectedVariableChange(InspectedVariable to, Location loc){
        this.typeName = to.getValueType();
        this.name = to.getIdentifier();
        this.fromInstance = null;
        this.fromValue = "<EMPTY>";
        this.toInstance = to;
        this.toValue = to.getValueString();
        this.location = loc;
    }

    public String toString(){
        StringBuffer builder = new StringBuffer();
        builder.append("name='").append(this.name)
            .append("' type='").append(this.typeName)
            .append("' fromValue='").append(this.fromValue)
            .append("' toValue='").append(this.toValue);
        if(this.location != null)
            builder.append("' location='").append(this.location);
        builder.append("'");
        return builder.toString();
    }
}
