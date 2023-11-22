package com.github.universetraveller.java.inspect.reporter;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.github.universetraveller.java.inspect.model.Report;
import com.github.universetraveller.java.inspect.model.Reporter;

public class FileReporter implements Reporter {
    protected File outputFile;
    protected Report report;
    public void setReport(Report report) {
        this.report = report;
    }
    public Report getReport() {
        return report;
    }
    public File getOutputFile(){
        return outputFile;
    }
    public Report generateReport() throws IllegalStateException {
        if(this.report == null)
            throw new IllegalStateException("Report should not be null");
        try{
            FileUtils.writeStringToFile(outputFile, report.buildString());
        }catch(IOException e){
            throw new IllegalStateException(e);
        }
        return this.report;
    }
}
