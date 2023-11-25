package com.github.universetraveller.java.inspect.model;

public class InspectedOutput extends InspectedEvent{
    private String content;
    private String name;

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public final static int STDOUT = 0;
    public final static int STDERR = 1;
    public static InspectedOutput getInstance(Inspector inspector, String content, int level){
        InspectedOutput instance = new InspectedOutput();
        InspectedEvent.init(instance, inspector, null);
        instance.name = level == STDOUT ? "StdOut" : "StdErr";
        instance.content = content;
        instance.buildString();
        return instance;
    }

    protected String internalBuildString(){
        return new StringBuffer()
                     .append("<").append(this.name).append(">")
                     .append(this.content)
                     .append("</").append(this.name).append(">")
                     .toString();
    }
}
