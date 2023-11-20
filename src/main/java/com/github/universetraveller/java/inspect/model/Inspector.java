package com.github.universetraveller.java.inspect.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.StepRequest;

public abstract class Inspector {
    protected String mainClass;
    protected String[] mainArgs;
    protected String classPath;
    protected int[] breakPointLines;
    protected StepRequest mainStepHandler;
    protected MethodEntryRequest globalEntryRequest;
    protected MethodExitRequest globalExitRequest;
    protected HashSet<Long> threadIds;
    protected boolean inspectMethod;
    protected boolean inspectGlobalMethod;
    protected boolean inspectImplicitMethod;
    public boolean isInspectImplicitMethod() {
        return inspectImplicitMethod;
    }

    protected boolean inspectSpectra;
    protected boolean inspectException;
    protected boolean inspectOutput;
    protected boolean inspectFields;
    protected boolean inspectVariables;
    public boolean isInspectVariables() {
        return inspectVariables;
    }

    protected boolean inspectVariableChanges;
    public boolean isInspectVariableChanges() {
        return inspectVariableChanges;
    }

    protected boolean oneShotBreakPoint;
    protected boolean deepStep;
    protected boolean accurateStep;
    public boolean isOneShotBreakPoint() {
        return oneShotBreakPoint;
    }
    public boolean isInspectOutput() {
        return inspectOutput;
    }

    protected int maxFrameCountToInspect;
    public int getMaxFrameCountToInspect() {
        return maxFrameCountToInspect;
    }

    protected String classFilterPattern;
    protected ConcurrentHashMap<Method, Stack<InspectedMethodEntry>> monitoredMethods;
    protected VirtualMachine vm;
    protected String[] classFilter;
    protected String[] classExclusionFilter; 
    protected List<InspectedEvent> events;
    protected Map<LocalVariable, Value> variableMap;
    protected Map<String, List<String>> methodsToInspect;
    protected String baseSrcDir;
    protected long startTimeStamp;
    protected Calendar calendar;
    protected long eventId;


    protected String mainValue;
    protected Logger logger;
    protected InspectedMethodInvoking globalInvoking;

    public InspectedMethodInvoking getGlobalInvoking() {
        return globalInvoking;
    }
    public void setGlobalInvoking(InspectedMethodInvoking globalInvoking) {
        this.globalInvoking = globalInvoking;
    }
    public VirtualMachine getVm() {
        return vm;
    }
    public void setVm(VirtualMachine vm) {
        this.vm = vm;
    }
    public void setLogLevel(Level level){
        this.logger.setLevel(level);
    }

    public Logger getLogger(){
        return this.logger;
    }

    protected void init(){
        this.buildMainValue();
        this.eventId = -1;
        this.logger = Logger.getLogger(Inspector.class.getName());
        this.logger.setLevel(Level.CONFIG);
        this.events = new ArrayList<>();
        this.variableMap = new ConcurrentHashMap<>();
        this.monitoredMethods = new ConcurrentHashMap<>();
        this.calendar = Calendar.getInstance();
    }
    protected void buildMainValue(){
        StringBuilder builder = new StringBuilder(this.mainClass);
        for(String arg : this.mainArgs)
            builder.append(arg).append(" ");
        this.mainValue = builder.toString();
    }
    protected VirtualMachine launchVirtualMachine() throws IOException, IllegalConnectorArgumentsException, VMStartException {
        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
        Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments.get("main").setValue(this.mainValue);
        arguments.get("options").setValue("-cp " + this.classPath);
        this.logger.config(String.format("JVM invoking arguments: %s %s", arguments.get("main"), arguments.get("options")));
        VirtualMachine vm = launchingConnector.launch(arguments);
        this.startTimeStamp = this.calendar.getTimeInMillis();
        return vm;
    }

    public long getNextId(){
        this.eventId += 1;
        return this.eventId;
    }

    public long getRunningTime(){
        return this.calendar.getTimeInMillis() - this.startTimeStamp;
    }

    public void addEvent(InspectedEvent event){
        if(this.eventId != this.events.size()){
            this.logger.warning(String.format("Event id unmatched: %s and %; reset eventId", this.eventId, this.events.size()));
            this.eventId = this.events.size()-1;
        }
        this.events.add(event);
    }

    protected ClassPrepareRequest makeClassPrepareRequest(){
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        if(this.classFilter != null)
            for(String filterPattern : this.classExclusionFilter)
                classPrepareRequest.addClassExclusionFilter(filterPattern);
        if(this.classExclusionFilter != null)
            for(String filterPattern : this.classFilter)
                classPrepareRequest.addClassFilter(filterPattern);
        this.logger.config(String.format("ClassPrepareRequest %s is made", classPrepareRequest));
        return classPrepareRequest;
    }

    protected void addBreakPoint(Location location){
        BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
        bpReq.enable();
    }
    public void tryToInspectClass(ClassPrepareEvent event){
        String nClz = event.referenceType().name();
        this.logger.info(String.format("Try to inspect <%s>", nClz));
        if(nClz.equals(mainClass) && breakPointLines.length > 0){
            ClassType classType = (ClassType) event.referenceType();
            for(int lineNumber: breakPointLines) {
                try{
                    Location location = classType.locationsOfLine(lineNumber).get(0);
                    this.addBreakPoint(location);
                    this.logger.info(String.format("Add breakpoint for %s at %s # %s", nClz, location, tryToFindSource(location)));
                }catch(IndexOutOfBoundsException e){
                    this.logger.warning(String.format("Cannot find location for %s at line %s; so skip it", nClz, lineNumber));
                }catch(AbsentInformationException f){
                    this.logger.warning(String.format("Cannot find location for %s at line %s; so skip it", nClz, lineNumber));
                }
            }
            tryToInspectFields(classType);
        }else if(this.methodsToInspect.keySet().contains(nClz)){
           ClassType classType = (ClassType) event.referenceType(); 
           for(String nMet : this.methodsToInspect.get(nClz)){
                for(Method sMet : classType.methodsByName(nMet)){
                    Location location = sMet.location();
                    this.addBreakPoint(location);
                    this.logger.info(String.format("Add breakpoint for %s at %s # %s", nClz, location, tryToFindSource(location)));
                }
            }
            tryToInspectFields(classType);
        }
    }

    protected void tryToInspectFields(ClassType classType) {
        if(!this.inspectFields)
            return;
        for(Field field : classType.visibleFields()){
            AccessWatchpointRequest awr = vm.eventRequestManager().createAccessWatchpointRequest(field);
            awr.enable();
            ModificationWatchpointRequest mwr = vm.eventRequestManager().createModificationWatchpointRequest(field);
            mwr.enable();
        }
    }
    protected String tryToFindSource(Location location) {
        String result = "<UNKNOWN>";
        try{
            result = FileUtils.readFileToString(new File(this.baseSrcDir + location.sourcePath()));
            return result.split("\n")[location.lineNumber() - 1];
        }catch(AbsentInformationException e){
            result = "<NO_INFO>";
        }catch(IOException e){
            result = "<FILE_NOT_FOUND>";
        }
        return result;
    }
    public void makeMethodEntryRequest(BreakpointEvent event) {
        if(this.inspectMethod){

            this.logger.info("Add method inspector");

            MethodEntryRequest request0 = vm.eventRequestManager().createMethodEntryRequest();
            request0.addClassFilter(this.classFilterPattern);
            request0.enable();

            MethodExitRequest request1 = vm.eventRequestManager().createMethodExitRequest();
            request1.addClassFilter(this.classFilterPattern);
            request1.enable();

            if(this.inspectGlobalMethod){
                this.logger.info("Add global method inspector");

                MethodEntryRequest request2 = vm.eventRequestManager().createMethodEntryRequest();
                request2.addClassExclusionFilter(this.classFilterPattern);
                this.globalEntryRequest = request2;
                request2.enable();

                MethodExitRequest request3 = vm.eventRequestManager().createMethodExitRequest();
                request3.addClassExclusionFilter(this.classFilterPattern);
                this.globalExitRequest = request3;
                request3.enable();
            }
        }
    }
    public boolean threadIsBusy(long id){
        return this.threadIds.contains(id);
    }
    public void threadToBusy(ThreadReference t){
        this.threadIds.add(t.uniqueID());
    }
    public void makeStepRequest(BreakpointEvent event) {
        if(!this.inspectSpectra)
            return;
        ThreadReference t = event.thread();
        if(this.threadIsBusy(t.uniqueID()))
            return;
        int stepSize = this.accurateStep ? StepRequest.STEP_MIN : StepRequest.STEP_LINE;
        int stepDepth = this.deepStep ? StepRequest.STEP_INTO : StepRequest.STEP_OVER;
        StepRequest sr = vm.eventRequestManager().createStepRequest(t, stepSize, stepDepth);
        sr.addClassFilter(this.classFilterPattern);

        if(!this.inspectGlobalMethod)
            for(String filterPattern : this.classExclusionFilter)
                sr.addClassExclusionFilter(filterPattern);

        sr.enable();

        this.logger.info("Add spectra insepctor");

        this.threadToBusy(t);
    }
    public void makeExceptionRequest(BreakpointEvent event) {
        if(!this.inspectException)
            return;
        this.logger.info("Add exception inspector");
        vm.eventRequestManager().createExceptionRequest(null, true, true).enable();
    }

    public EventSet getVMEventSet() throws InterruptedException{
        return this.vm.eventQueue().remove();
    }
    public void resume(){
        this.vm.resume();
    }

    public void beforeResume(){
        // Do nothing there
        // debuggers should override it
    }

    public InputStream getVMInputStream(){
        return this.vm.process().getInputStream();
    }

    public InputStream getVMErrorStream(){
        return this.vm.process().getErrorStream();
    }

    public boolean isGlobalMethodEntryRequest(EventRequest r){
        return this.globalEntryRequest.equals(r);
    }

    public boolean isGlobalMethodExitRequest(EventRequest r){
        return this.globalExitRequest.equals(r);
    }

    public boolean canHandleMethodEntry(){
        return this.globalEntryRequest.isEnabled();
    }

    public boolean canHandleMethodExit(){
        return this.globalExitRequest.isEnabled();
    }

    public void disableGlobalEntryHandler(){
        this.globalEntryRequest.disable();
    }

    public void enableGlobalEntryHandler(){
        this.globalEntryRequest.enable();
    }

    public void addHandlingMethod(Method method) {
        // inspectors can override this method
        this.globalEntryRequest.putProperty("HANDLING", method);
    }

    public void removeHandlingMethod(Method method){
        // inspectors can override this method
        this.globalEntryRequest.putProperty("HANDLING", null);
    }

    public boolean isHandlingGlobalMethod(Method method){
        return this.globalEntryRequest.getProperty("HANDLING").equals(method);
    }
    public void registerMethod(InspectedMethodEntry entry){
        Method met = entry.getMethodInstance();
        monitoredMethods.putIfAbsent(met, new Stack<>());
        monitoredMethods.get(met).push(entry);
    }
    
    public InspectedMethodEntry finishMethod(InspectedMethodExit event){
        Method met = event.getMethodInstance();
        InspectedMethodEntry entry = null;
        if(monitoredMethods.containsKey(met)){
            entry = monitoredMethods.get(met).pop();
            if(monitoredMethods.get(met).empty())
                monitoredMethods.remove(met);
        }
        return entry;
    }
    public List<InspectedVariableChange> updateVariableMap(StackFrame targetFrame) throws AbsentInformationException{
        return updateVariableMap(targetFrame, new ArrayList<>());
    }

    public List<InspectedVariableChange> updateVariableMap(StackFrame targetFrame, List<InspectedVariableChange> collected) throws AbsentInformationException{
        for(Map.Entry<LocalVariable, Value> entry : targetFrame.getValues(targetFrame.visibleVariables()).entrySet()){
            LocalVariable variable = entry.getKey();
            Value value = entry.getValue();
            InspectedVariableChange change = null;
            if(this.variableMap.containsKey(variable)){
                Value fromValue = this.variableMap.get(variable);
                if(fromValue.equals(value))
                    continue;
                change = new InspectedVariableChange(new InspectedVariable(variable, fromValue), new InspectedVariable(variable, value), null);
            }else{
                change = new InspectedVariableChange(new InspectedVariable(variable, value), null);
            }
            this.variableMap.put(variable, value);
            collected.add(change);
        }
        return collected;
    }
    public List<InspectedVariableChange> updateVariableMap(List<StackFrame> targetFrames) {
        List<InspectedVariableChange> collected = new ArrayList<>();
        for(int i = 0; i < this.maxFrameCountToInspect && i < targetFrames.size(); i++){
            try{
                updateVariableMap(targetFrames.get(i), collected);
            }catch(AbsentInformationException e){
                // ignore it
                this.logger.warning("AbsentInformationException occurs when updating variable map; skip");
            }
        }
        return collected;
    }
    public abstract void execute(InspectorRunner runner);
}
