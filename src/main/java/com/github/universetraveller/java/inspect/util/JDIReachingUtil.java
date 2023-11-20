package com.github.universetraveller.java.inspect.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.universetraveller.java.inspect.model.InspectedVariable;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;

public class JDIReachingUtil {
    public static List<InspectedVariable> inspectVariables(StackFrame frame) throws AbsentInformationException{
        return inspectVariables(frame, new ArrayList<>());
    }
    public static List<InspectedVariable> inspectVariables(StackFrame frame, List<InspectedVariable> variablesList) throws AbsentInformationException{
        Map<LocalVariable, Value> variables = frame.getValues(frame.visibleVariables());
        for(Map.Entry<LocalVariable, Value> variable : variables.entrySet())
            variablesList.add(new InspectedVariable(variable.getKey(), variable.getValue()));
        return variablesList;
    }
    public static List<InspectedVariable> inspectVariables(List<StackFrame> frames) throws AbsentInformationException{
        List<InspectedVariable> variables = new ArrayList<>();
        for(StackFrame frame : frames)
            inspectVariables(frame, variables);
        return variables;
    }    
    public static List<String> buildStackTrace(List<StackFrame> frames)throws AbsentInformationException{
        List<String> traces = new ArrayList<>();
        for(StackFrame frame : frames)
            traces.add(String.format("at %s(%s:%s)", frame.location().method(), frame.location().sourcePath(), frame.location().lineNumber()));
        return traces;
    }
}
