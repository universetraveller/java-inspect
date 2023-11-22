package com.github.universetraveller.java.inspect.model;

import java.util.List;

public interface Report {
    public void fillReport();
    public List<ReportElement> filter(Filter filter);
    public String buildString();
    public List<ReportElement> getElements();
    public void applyFilter(Filter filter);
}