package com.github.universetraveller.java.inspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.universetraveller.java.inspect.inspector.DefaultInspectorRunner;
import com.github.universetraveller.java.inspect.inspector.MainInspector;
import com.github.universetraveller.java.inspect.model.InspectedEvent;

public class DefaultMain {
    public static List<InspectedEvent> execute(String[] args){
        Map<String, List<String>> methodMap = new HashMap<>();
        for(String method : args[1].split("#")){
            String[] pair = method.split("::");
            methodMap.putIfAbsent(pair[0], new ArrayList<>());
            methodMap.get(pair[0]).add(pair[1]);
        }
        long timeout = -1;
        if(args.length >= 5)
            timeout = Integer.parseInt(args[4]);
        MainInspector inspector = MainInspector.getInstance()
                                                .configMainClass(args[0])
                                                .configMainArgs(new String[]{args[1]})
                                                .configClassPath(args[2])
                                                .configClassFilterPattern(args[3])
                                                .configMethodToInspect(methodMap)
                                                .configMaxTimeRunning(timeout);
        inspector.execute(new DefaultInspectorRunner());
        return inspector.getEvents();
    }

    public static void main(String[] args){
        for(InspectedEvent event : execute(args))
            System.out.println(event.buildString());
    }
}
