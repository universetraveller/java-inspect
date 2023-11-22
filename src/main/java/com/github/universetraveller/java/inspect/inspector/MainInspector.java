package com.github.universetraveller.java.inspect.inspector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.universetraveller.java.inspect.model.Inspector;
import com.github.universetraveller.java.inspect.model.InspectorRunner;

public class MainInspector extends Inspector {

    public MainInspector(String mainClass, String[] mainArgs, String classPath, int[] breakPointLines,
            boolean inspectMethod, boolean inspectGlobalMethod, boolean inspectImplicitMethod, boolean inspectSpectra,
            boolean inspectException, boolean inspectOutput, boolean inspectFields, boolean inspectVariables,
            boolean inspectVariableChanges, boolean oneShotBreakPoint, boolean deepStep, boolean accurateStep,
            int maxFrameCountToInspect, String classFilterPattern, String[] classFilter, String[] classExclusionFilter,
            String baseSrcDir, Map<String, List<String>> methodsToInspect) {
        super();
        this.mainClass = mainClass;
        this.mainArgs = mainArgs;
        this.classPath = classPath;
        this.breakPointLines = breakPointLines;
        this.inspectMethod = inspectMethod;
        this.inspectGlobalMethod = inspectGlobalMethod;
        this.inspectImplicitMethod = inspectImplicitMethod;
        this.inspectSpectra = inspectSpectra;
        this.inspectException = inspectException;
        this.inspectOutput = inspectOutput;
        this.inspectFields = inspectFields;
        this.inspectVariables = inspectVariables;
        this.inspectVariableChanges = inspectVariableChanges;
        this.oneShotBreakPoint = oneShotBreakPoint;
        this.deepStep = deepStep;
        this.accurateStep = accurateStep;
        this.maxFrameCountToInspect = maxFrameCountToInspect;
        this.classFilterPattern = classFilterPattern;
        this.classFilter = classFilter;
        this.classExclusionFilter = classExclusionFilter;
        this.baseSrcDir = baseSrcDir;
        this.methodsToInspect = methodsToInspect;
    }

    public static MainInspector getInstance() {
        return new MainInspector();
    }

    public MainInspector(){
        this(null,
                new String[0],
                "",
                new int[0],
                true,
                true,
                false,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                1,
                "*",
                new String[0],
                new String[] { "org.junit.*", "java.*", "sun.*", "jdk.*", "junit.*" },
                "./",
                new HashMap<>());
    }

    public MainInspector configMainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public MainInspector configMainArgs(String[] args) {
        this.mainArgs = args;
        return this;
    }

    public MainInspector configClassPath(String classPath) {
        this.classPath = classPath;
        return this;
    }

    public MainInspector configBreakPointLines(int[] lines) {
        this.breakPointLines = lines;
        return this;
    }

    public MainInspector configInspectMethod(boolean config) {
        this.inspectMethod = config;
        return this;
    }

    public MainInspector configInspectGlobalMethod(boolean config) {
        this.inspectGlobalMethod = config;
        return this;
    }

    public MainInspector configInspectImplicitMethod(boolean config) {
        this.inspectImplicitMethod = config;
        return this;
    }

    public MainInspector configInspectSpectra(boolean config) {
        this.inspectSpectra = config;
        return this;
    }

    public MainInspector configInspectException(boolean config) {
        this.inspectException = config;
        return this;
    }

    public MainInspector configInspectOutput(boolean config) {
        this.inspectOutput = config;
        return this;
    }

    public MainInspector configInspectFields(boolean config) {
        this.inspectFields = config;
        return this;
    }

    public MainInspector configInspectVariables(boolean config) {
        this.inspectVariables = config;
        return this;
    }

    public MainInspector configInspectVariableChanges(boolean config) {
        this.inspectVariableChanges = config;
        return this;
    }

    public MainInspector configOneShotBreakPoint(boolean config) {
        this.oneShotBreakPoint = config;
        return this;
    }

    public MainInspector configDeepStep(boolean config) {
        this.deepStep = config;
        return this;
    }

    public MainInspector configAccurateStep(boolean config) {
        this.accurateStep = config;
        return this;
    }

    public MainInspector configMaxFrameCountToInspect(int config) {
        this.maxFrameCountToInspect = config;
        return this;
    }

    public MainInspector configClassFilterPattern(String config) {
        this.classFilterPattern = config;
        return this;
    }

    public MainInspector configClassFilter(String[] config) {
        this.classFilter = config;
        return this;
    }

    public MainInspector configClassExclusionFilter(String[] config) {
        this.classExclusionFilter = config;
        return this;
    }

    public MainInspector configBaseSrcDir(String config) {
        this.baseSrcDir = config;
        return this;
    }

    public MainInspector configMethodToInspect(Map<String, List<String>> config) {
        this.methodsToInspect = config;
        return this;
    }

    private void validateFields() throws IllegalArgumentException{
        if(this.mainClass == null || this.vm == null)
            throw new IllegalArgumentException("Unexpected null argument(s)");
    }

    private void prepareToRun() throws Exception {
        this.validateFields();
        this.vm = this.launchVirtualMachine();
        this.makeClassPrepareRequest().enable();
    }

    public void execute(InspectorRunner runner) {
        try {
            this.prepareToRun();
            runner.run(this);
        } catch (Exception e) {
            this.logger.severe("MainInspector process is unexpectedly stopped");
            e.printStackTrace();
        }
    }
}