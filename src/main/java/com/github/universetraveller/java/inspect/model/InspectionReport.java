package com.github.universetraveller.java.inspect.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InspectionReport implements Report {
    private Inspector inspector;
    private List<ReportElement> elements;
    private Map<Long, Class> idMap;
    public InspectionReport(Inspector inspector){
        this.inspector = inspector;
        this.elements = new ArrayList<>();
        this.idMap = new HashMap<>();
    }
    public List<ReportElement> getElements() {
        return elements;
    }
    public void fillReport(){
        for(InspectedEvent event : this.inspector.getEvents()){
            this.elements.add(buildElement(event));
            // TODO not fully implemented
            this.idMap.put(event.getId(), event.getClass());
        }
    }
    public List<ReportElement> filter(Filter filter){
        // TODO is not implemented now
        return elements;
    }
    public void applyFilter(Filter filter){
        this.elements = filter(filter);
    }
    private ReportElement buildElement(InspectedEvent event){
        return new ReportElement(event, event.getId()) {
            public String toString(){
                return event.buildString();
            }
        };
    }
    public String buildString(){
        StringBuffer builder = new StringBuffer();
        builder.append("<InspectionReport>\n");
        for(ReportElement element : this.elements)
            builder.append(element.toString());
        builder.append("\n</InspectionReport>");
        return builder.toString();
    }
    public String toString(){
        return this.buildString();
    }
}
