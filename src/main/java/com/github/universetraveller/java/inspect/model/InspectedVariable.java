package com.github.universetraveller.java.inspect.model;

import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Value;

public class InspectedVariable {
    private LocalVariable variableInstance;
    private Value valueInstance;
    private String identifier;
    private String valueString;
    private String valueType;
    public String getValueType() {
        return valueType;
    }
    private String signature;
    public InspectedVariable(LocalVariable localVariable, Value value){
        this.variableInstance = localVariable;
        this.valueInstance = value;
        this.identifier = localVariable.toString();
        this.signature = String.format("%s#%s", localVariable.genericSignature(), localVariable.signature());
        this.valueString = value.toString();
        if(!localVariable.typeName().isEmpty())
            this.valueType = localVariable.typeName();
        else
            this.valueType = value.type().toString();
    }
    public InspectedVariable(Field field, Value value){
        this.valueInstance = value;
        this.identifier = field.toString();
        this.variableInstance = null;
        this.valueString = value.toString();
        this.valueType = field.typeName().isEmpty() ? value.type().toString() : field.typeName();
    }
    public String toString(){
        return String.format("<Variable name='%s' type='%s' value='%s' signature='%s'/>", this.identifier, this.valueType, this.valueString, this.signature);
    }
    public LocalVariable getVariableInstance() {
        return variableInstance;
    }
    public void setVariableInstance(LocalVariable variableInstance) {
        this.variableInstance = variableInstance;
    }
    public Value getValueInstance() {
        return valueInstance;
    }
    public void setValueInstance(Value valueInstance) {
        this.valueInstance = valueInstance;
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getValueString() {
        return valueString;
    }
    public void setValueString(String valueString) {
        this.valueString = valueString;
    }
    
}
