package com.github.universetraveller.java.inspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.universetraveller.java.inspect.inspector.DefaultInspectorRunner;
import com.github.universetraveller.java.inspect.inspector.MainInspector;
import com.github.universetraveller.java.inspect.model.InspectedEvent;

public class DefaultMain {
    public static void main(String[] args){
        Map<String, List<String>> methodMap = new HashMap<>();
        for(String method : args[1].split("#")){
            String[] pair = method.split("::");
            methodMap.putIfAbsent(pair[0], new ArrayList<>());
            methodMap.get(pair[0]).add(pair[1]);
        }
        MainInspector inspector = MainInspector.getInstance()
                                                .configMainClass(args[0])
                                                .configMainArgs(new String[]{args[1]})
                                                .configClassPath(args[2])
                                                .configClassFilterPattern(args[3])
                                                .configMethodToInspect(methodMap);
        inspector.execute(new DefaultInspectorRunner());
        for(InspectedEvent event : inspector.getEvents())
            System.out.println(event.buildString());
    }
}