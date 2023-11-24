package com.github.universetraveller.java.inspect.downstream.fl;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.github.universetraveller.java.inspect.DefaultMain;
import com.github.universetraveller.java.inspect.model.InspectedBreakpoint;
import com.github.universetraveller.java.inspect.model.InspectedEvent;
import com.github.universetraveller.java.inspect.model.InspectedException;
import com.github.universetraveller.java.inspect.model.InspectedFieldAccess;
import com.github.universetraveller.java.inspect.model.InspectedFieldModification;
import com.github.universetraveller.java.inspect.model.InspectedMethod;
import com.github.universetraveller.java.inspect.model.InspectedMethodEntry;
import com.github.universetraveller.java.inspect.model.InspectedMethodExit;
import com.github.universetraveller.java.inspect.model.InspectedMethodInvoking;
import com.github.universetraveller.java.inspect.model.InspectedOutput;
import com.github.universetraveller.java.inspect.model.InspectedStep;
import com.github.universetraveller.java.inspect.model.InspectedVariableChange;
import com.github.universetraveller.java.inspect.model.InspectedField;

class AnalysisUnit {
    String lineIdentifier;
    String unitIdentifier;
    Set<String> failingTestsExecuteIt;
    Set<String> passingTestsExecuteIt;
    double score;
    double lineScore;
    double finalScore;
    public AnalysisUnit(String lineIdentifier, String unitIdentifier) {
        this.lineIdentifier = lineIdentifier;
        this.unitIdentifier = unitIdentifier;
        this.failingTestsExecuteIt = new HashSet<>();
        this.passingTestsExecuteIt = new HashSet<>();
        this.score = 0.0;
        this.lineScore = 0.0;
    }
    public String getLineIdentifier() {
        return lineIdentifier;
    }
    public void setLineIdentifier(String lineIdentifier) {
        this.lineIdentifier = lineIdentifier;
    }
    public String getUnitIdentifier() {
        return unitIdentifier;
    }
    public void setUnitIdentifier(String unitIdentifier) {
        this.unitIdentifier = unitIdentifier;
    }
    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }
    public void addFailingTest(String name){
        this.failingTestsExecuteIt.add(name);
    }
    public void addPassingTest(String name){
        this.passingTestsExecuteIt.add(name);
    }
    public int getFailingTestsCount(){
        return this.failingTestsExecuteIt.size();
    }
    public int getPassingTestsCount(){
        return this.passingTestsExecuteIt.size();
    }
    public void addTest(String name, boolean isFailingTest){
        if(isFailingTest)
            this.failingTestsExecuteIt.add(name);
        else
            this.passingTestsExecuteIt.add(name);
    }
    public double getLineScore() {
        return lineScore;
    }
    public void setLineScore(double lineScore) {
        this.lineScore = lineScore;
    }
    public double getFinalScore() {
        return finalScore;
    }
    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }
}

class LinePack {
    InspectedEvent step;
    List<InspectedEvent> packedEvents;
    public LinePack(InspectedEvent step, List<InspectedEvent> packedEvents) {
        this.step = step;
        this.packedEvents = packedEvents;
    }
    public InspectedEvent getStep() {
        return step;
    }
    public void setStep(InspectedEvent step) {
        this.step = step;
    }
    public List<InspectedEvent> getPackedEvents() {
        return packedEvents;
    }
    public void setPackedEvents(List<InspectedEvent> packedEvents) {
        this.packedEvents = packedEvents;
    }
}

/*
 * This class is basic implementation and still remains some optimation.
 * Some optimization examples:
 *      1. Assign different weight for each type of events
 *      2. Leverage more parameters to evaluate the score like frameDepth, executionTime in method event
 */
public class SimpleOchiaiFaultLocalization {
    /*
     * Ochiai(e) = f(e) / sqrt((f(e) + p(e)) * f(all))
     * f(arg) -> Number of failing tests executing arg  
     * p(arg) -> Number of passing tests executing arg  
     */
    protected static double ochiai(long fe, long fne, long pe, long pne){
        if(fe + fne == 0 || fe + pe == 0)
            return 0.0;
        return fe / Math.sqrt((fe + fne) * (fe + pe));
    }

    public static double computeScore(long fe, long fne, long pe, long pne){
        return ochiai(fe, fne, pe, pne);
    }

    Set<String> failingTests;
    Set<String> passingTests;
    Map<String, Map<String, AnalysisUnit>> results;
    List<AnalysisUnit> units;
    String cwd;
    String groupPattern;
    String testRegularExpression;
    Pattern testNamePattern;
    String classPath;
    String testRunner;
    Logger logger;
    String reportDir;
    long failingNum;
    long passingNum;
    double lineScoreWeight;
    final static Pattern exceptionPattern = Pattern.compile("<Exception type='(.+)' occurAt");
    final static Pattern instancePattern = Pattern.compile("instance of (.+)\\(.+\\)");
    final static Pattern methodPattern = Pattern.compile("<Method(Entry|Invoking|Exit) method='(.+\\(.+\\))' location=");
    final static Pattern variableChangeNamePattern = Pattern.compile("(.*) in .*' type=");
    public SimpleOchiaiFaultLocalization(){
        this.failingTests = new HashSet<>();
        this.passingTests = new HashSet<>();
        this.results = new ConcurrentHashMap<>();
        this.units = new ArrayList<>();
        this.cwd = "./";
        this.reportDir = cwd;
        this.groupPattern = "*";
        this.testRegularExpression = "([a-zA-Z][a-zA-Z0-9$_]+)\\(([a-zA-Z][a-zA-Z0-9.$_]+)\\)";
        this.testNamePattern = Pattern.compile(testRegularExpression);
        this.classPath = null;
        this.testRunner = null;
        this.failingNum = 0;
        this.passingNum = 0;
        this.lineScoreWeight = 0.75;
        this.logger = Logger.getLogger(SimpleOchiaiFaultLocalization.class.getName());
        this.logger.setLevel(Level.INFO);
    }

    public static SimpleOchiaiFaultLocalization getInstance(){
        return new SimpleOchiaiFaultLocalization();
    }

    public SimpleOchiaiFaultLocalization configCwd(String config){
        this.cwd = config;
        return this;
    }

    public SimpleOchiaiFaultLocalization configGroupPattern(String config){
        this.groupPattern = config;
        return this;
    }

    public SimpleOchiaiFaultLocalization configClassPath(String config){
        this.classPath = config;
        return this;
    }

    public SimpleOchiaiFaultLocalization configTestRunner(String config){
        this.testRunner = config;
        return this;
    }

    public SimpleOchiaiFaultLocalization configReportDir(String config){
        this.reportDir = config;
        return this;
    }

    public String checkAndTransformTestFormat(String test) throws IOException {
        Matcher matches = this.testNamePattern.matcher(test);
        if(matches.find()){
            return String.format("%s::%s", matches.group(1), matches.group(2));
        }
        throw new IOException("Test " + test + " does not match regular expression " + this.testRegularExpression);

    }

    public void fillTests() throws IOException {
        String fileFailingTests = FileUtils.readFileToString(new File(this.cwd + "/failingTests"));
        for(String eachFailingTest : fileFailingTests.split(System.lineSeparator()))
            this.failingTests.add(checkAndTransformTestFormat(eachFailingTest));
        String filePassingTests = FileUtils.readFileToString(new File(this.cwd + "/passingTests"));
        for(String eachPassingTests : filePassingTests.split(System.lineSeparator()))
            this.passingTests.add(checkAndTransformTestFormat(eachPassingTests));
        this.failingNum = this.failingTests.size();
        this.passingNum = this.passingTests.size();
        this.logger.info(String.format("Found %s failing tests, %s passing tests", this.failingNum, this.passingNum));
    }
    public void check() throws IllegalArgumentException {
        if(!new File(this.cwd + "/failingTests").exists())
            throw new IllegalArgumentException("File failingTests is needed");
        if(!new File(this.cwd + "/passingTests").exists())
            throw new IllegalArgumentException("File passingTests is needed");
        if(this.classPath == null)
            throw new IllegalArgumentException("Should configurate classpath");
        if(this.testRunner == null)
            throw new IllegalArgumentException("Should configurate testRunner");
    }

    /*
     * Collects events from the first step or breakpoint (excluded) to the last step
     */
    public static List<InspectedEvent> collectRelevantEvents(List<InspectedEvent> allEvents) {
        List<InspectedEvent> relevantEvents = new ArrayList<>();
        boolean collecting = false;
        InspectedEvent lastStep = findLastStep(allEvents);
        for(InspectedEvent event : allEvents){
            if(collecting){
                if(event instanceof InspectedOutput)
                    continue;
                relevantEvents.add(event);
                if(event.equals(lastStep) || event.getId() == lastStep.getId())
                    break;
            }else if(event instanceof InspectedStep){
                collecting = true;
                relevantEvents.add(event);
            }else if(event instanceof InspectedBreakpoint){
                // actually is not necessary because I analyze linepack
                collecting = true;
            }
        }
        return relevantEvents;
    }

    public static InspectedEvent findLastStep(List<InspectedEvent> allEvents) {
        ListIterator<InspectedEvent> iterator = allEvents.listIterator(allEvents.size());
        while(iterator.hasPrevious()){
            InspectedEvent event = iterator.previous();
            if(event instanceof InspectedStep)
                return event;
        }
        return allEvents.get(allEvents.size() - 1);
    }

    public List<LinePack> generateLinePack(List<InspectedEvent> events) {
        List<LinePack> packs = new ArrayList<>();
        if(events.isEmpty())
            return packs;
        List<InspectedEvent> cache = new ArrayList<>();
        InspectedEvent stepInstance = events.get(0);
        for(InspectedEvent event : events){
            if(event instanceof InspectedStep){
                packs.add(new LinePack(stepInstance, cache));
                cache = new ArrayList<>();
                stepInstance = event;
            }else{
                cache.add(event);
            }
        }
        if(!cache.isEmpty())
            packs.add(new LinePack(stepInstance, cache));
        return packs;
    }

    private void analyzeUnpackEventInLine(InspectedEvent event, String locationIdentifier, String testName,
            boolean isFailingTest) {
        String unitIdentifier = event.toString();
        if(event instanceof InspectedFieldAccess || event instanceof InspectedFieldModification)
            unitIdentifier = ((InspectedField)event).getFieldName();
        else if(event instanceof InspectedException)
            unitIdentifier = getExceptionName((InspectedException)event);
        else if(event instanceof InspectedMethodEntry || event instanceof InspectedMethodExit)
            unitIdentifier = getMethodName((InspectedMethod)event);
        else if(event instanceof InspectedMethodInvoking)
            unitIdentifier = getMethodName((InspectedMethod)(((InspectedMethodInvoking)event).getHead()));
        else
            this.logger.info("Found unknown type event " + event);
        this.results.get(locationIdentifier).putIfAbsent(unitIdentifier, new AnalysisUnit(locationIdentifier, unitIdentifier));
        this.results.get(locationIdentifier).get(unitIdentifier).addTest(testName, isFailingTest);
    }

    private String getMethodName(InspectedMethod event) {
        String name = "<UNKNOWN_METHOD>";
        try{
            name = event.getMethodInstance().toString();
        }catch(Exception e){
            name = event.toString();
            Matcher matcher = methodPattern.matcher(name);
            if(matcher.find()){
                name = matcher.group(2);
            }else{
                name = name.replace("<MethodInvoking", "").replace("<MethodEntry", "").replace("<MethodExit", "").replace(" method='", "");
                name = name.substring(0, name.indexOf("'"));
            }
        }
        return name;
    }

    public static String getExceptionName(InspectedException event) {
        String name = "<UNKNOWN_EXCEPTION>";
        try{
            name = event.getExceptionReference().toString();
        }catch(Exception e){
            name = event.toString();
            Matcher matcher = exceptionPattern.matcher(name);
            if(matcher.find()){
                name = matcher.group(1);
            } else{
                name = name.replace("<Exception type='", "");
                name = name.substring(0, name.indexOf("'"));
            }
        }
        if(name.startsWith("instance of")){
            Matcher matcher = instancePattern.matcher(name);
            if(matcher.find())
                name = matcher.group(1);
        }else{
            name = name.replace("instance of ", "");
            name = name.substring(0, name.indexOf("("));
        }
        return name;
    }

    private void analyzeLinePack(LinePack pack, String testName, boolean isFailingTest) {
        InspectedEvent step = pack.getStep();
        String locationIdentifier = step.toString();
        if(step instanceof InspectedStep)
            locationIdentifier = ((InspectedStep) step).getLocation().toString();
        results.putIfAbsent(locationIdentifier, new HashMap<>());
        results.get(locationIdentifier).putIfAbsent("LINE", new AnalysisUnit(locationIdentifier, "LINE"));
        results.get(locationIdentifier).get("LINE").addTest(testName, isFailingTest);
        if(step instanceof InspectedBreakpoint || step instanceof InspectedStep){
            // though check instance type multi-time, it makes code clean
            for(InspectedVariableChange change : ((InspectedBreakpoint)step).getVariableChanges()){
                String name = change.getName();
                Matcher matcher = variableChangeNamePattern.matcher(name);
                if(matcher.find())
                    name = matcher.group(1);
                else
                    name = name.split(" ")[0];
                results.get(locationIdentifier).putIfAbsent(name, new AnalysisUnit(locationIdentifier, name));
                results.get(locationIdentifier).get(name).addTest(testName, isFailingTest);
            }
        }
        for(InspectedEvent event : pack.getPackedEvents())
            analyzeUnpackEventInLine(event, locationIdentifier, testName, isFailingTest);
    }

    public void executeSingleTest(String testName, boolean isFailingTest) {
        boolean isFailingTest1 = this.failingTests.contains(testName);
        if(!isFailingTest1 && !this.passingTests.contains(testName)){
            this.logger.warning("Found " + testName + " not in tests sets; ignore it");
            return;
        }
        if(isFailingTest != isFailingTest1)
            this.logger.warning("Check failed that passed isFailingTest argument does not equal to runtime computation");
        this.logger.info("Try to execute " + testName);
        // To get the most accurate information I launch a new VM ro execute single test though it is high cost
        // executin it with multi-thread may ease the problem
        List<InspectedEvent> events = collectRelevantEvents(DefaultMain.execute(new String[]{this.testRunner, testName, this.classPath, this.groupPattern}));
        this.logger.info("Collected " + events.size() + " relevant events");
        for(LinePack pack : generateLinePack(events))
            analyzeLinePack(pack, testName, isFailingTest);
        this.logger.info(testName + " is analyzed");
    }

    public void execute() throws IllegalArgumentException, IOException {
        check();
        fillTests();
        long counter = 1;
        long total = this.failingNum + this.passingNum;
        for(String failingTestName : this.failingTests){
            this.logger.info("Running: " + counter + "/" + total);
            executeSingleTest(failingTestName, true);
            counter++;
        }
        for(String passTestName : this.passingTests){
            this.logger.info("Running: " + counter + "/" + total);
            executeSingleTest(passTestName, false);
            counter++;
        }
        buildUnits();
        writeReport(generateReport());
    }

    private void writeReport(List<String> report) throws IOException {
        StringBuffer builder = new StringBuffer();
        for(String line : report)
            builder.append(line).append("\n");
        FileUtils.writeStringToFile(new File(this.reportDir+"/OchiaiReport"), builder.toString());
    }

    private void buildUnits() {
        buildScoreForLines();
        rankScoreForLines();
    }

    private void rankScoreForLines() {
        Comparator<AnalysisUnit> comparator = new Comparator<AnalysisUnit>() {
            @Override
            public int compare(AnalysisUnit o1, AnalysisUnit o2){
                return Double.compare(o1.getFinalScore(), o2.getFinalScore());
            }
        };
        this.units.sort(comparator);
    }

    private double ochiaiForUnit(AnalysisUnit unit){
        long fe = unit.getFailingTestsCount();
        long pe = unit.getPassingTestsCount();
        return ochiai(fe, this.failingNum - fe, pe, this.passingNum - pe);
    }
    private void buildScoreForLines() {
        for(String line : this.results.keySet()){
            AnalysisUnit lineUnit = this.results.get(line).get("LINE");
            double lineScore = ochiaiForUnit(lineUnit);
            lineUnit.setScore(lineScore);
            lineUnit.setLineScore(lineScore);
            lineUnit.setFinalScore(lineScore);
            for(Map.Entry<String, AnalysisUnit> entry : this.results.get(line).entrySet()){
                AnalysisUnit entryValue = entry.getValue();
                this.units.add(entryValue);
                if(entry.getKey().equals("LINE"))
                    continue;
                entryValue.setLineScore(lineScore);
                entryValue.setScore(ochiaiForUnit(entryValue));
                entryValue.setFinalScore((lineScore * this.lineScoreWeight + entryValue.getScore() * (1 - this.lineScoreWeight)));
            }
        }
    }

    public List<String> generateReport(){
        List<String> reportContent = new ArrayList<>();
        ListIterator<AnalysisUnit> iterator = this.units.listIterator(this.units.size());
        while(iterator.hasPrevious()){
            AnalysisUnit unit = (AnalysisUnit)iterator.previous();
            if(unit.getUnitIdentifier().equals("LINE"))
                reportContent.add(String.format("%s$%s", unit.getLineIdentifier(), unit.getScore()));
            else
                reportContent.add(String.format("%s#%s$%s,%s,%s", unit.getLineIdentifier(), unit.getUnitIdentifier(), unit.getFinalScore(), unit.getScore(), unit.getLineScore()));
        }
        return reportContent;
    }
}
