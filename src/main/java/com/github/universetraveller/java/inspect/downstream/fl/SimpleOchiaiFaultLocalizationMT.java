package com.github.universetraveller.java.inspect.downstream.fl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SimpleOchiaiFaultLocalizationMT extends SimpleOchiaiFaultLocalization{

    public SimpleOchiaiFaultLocalizationMT(){
        this.failingTests = ConcurrentHashMap.newKeySet();
        this.passingTests = ConcurrentHashMap.newKeySet();
        this.testClassNames = ConcurrentHashMap.newKeySet();
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
        this.timeout = -1;
        this.counter = 0;
        this.total = 0;
        this.logger = Logger.getLogger(SimpleOchiaiFaultLocalization.class.getName());
        this.logger.setLevel(Level.INFO);
    }

    public static SimpleOchiaiFaultLocalizationMT getInstance(){
        return new SimpleOchiaiFaultLocalizationMT();
    }

    public SimpleOchiaiFaultLocalizationMT configCwd(String config){
        this.cwd = config;
        return this;
    }

    public SimpleOchiaiFaultLocalizationMT configGroupPattern(String config){
        this.groupPattern = config;
        return this;
    }

    public SimpleOchiaiFaultLocalizationMT configClassPath(String config){
        this.classPath = config;
        return this;
    }

    public SimpleOchiaiFaultLocalizationMT configTestRunner(String config){
        this.testRunner = config;
        return this;
    }

    public SimpleOchiaiFaultLocalizationMT configReportDir(String config){
        this.reportDir = config;
        return this;
    }

    public SimpleOchiaiFaultLocalizationMT configTimeout(long config){
        this.timeout = config;
        return this;
    }

    public SimpleOchiaiFaultLocalizationMT configSkipTimeoutTests(boolean config){
        this.skipTimeoutTests = config;
        return this;
    }

    public void execute() throws IllegalArgumentException, IOException {
        check();
        fillTests();
        this.total = this.failingNum + this.passingNum;
        this.failingTests.parallelStream().forEach(failingTestName -> {
            this.counter++;
            this.logger.info("Running: " + counter + "/" + total + " Expired tests: " + this.expiredTests);
            executeSingleTest(failingTestName, true);
        });
        this.passingTests.parallelStream().forEach(passTestName -> {
            this.counter++;
            this.logger.info("Running: " + counter + "/" + total + " Expired tests: " + this.expiredTests);
            executeSingleTest(passTestName, false);
        });
        buildUnits();
        writeReport(generateReport());
    }

}
