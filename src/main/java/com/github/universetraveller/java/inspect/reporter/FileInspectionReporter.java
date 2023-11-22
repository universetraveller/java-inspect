package com.github.universetraveller.java.inspect.reporter;

import java.io.File;

import com.github.universetraveller.java.inspect.model.Filter;
import com.github.universetraveller.java.inspect.model.InspectionReport;
import com.github.universetraveller.java.inspect.model.Inspector;

public class FileInspectionReporter extends FileReporter {
    Inspector inspector;
    public FileInspectionReporter(String outputFileString, Inspector inspector, Filter filter){
        this.outputFile = new File(outputFileString);
        this.inspector = inspector;
        this.makeReport();
        if(filter != null)
            this.report.applyFilter(filter);
    }
    public FileInspectionReporter(String filePath, Inspector inspector){
        this(filePath, inspector, null);
    }
    private void makeReport(){
        this.report = new InspectionReport(inspector);
        this.report.fillReport();
    }

}
