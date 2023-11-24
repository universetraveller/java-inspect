package com.github.universetraveller.java.inspect.maven;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.github.universetraveller.java.inspect.downstream.fl.SimpleOchiaiFaultLocalization;
import com.github.universetraveller.java.inspect.inspector.DefaultInspectorRunner;
import com.github.universetraveller.java.inspect.inspector.MainInspector;
import com.github.universetraveller.java.inspect.reporter.FileInspectionReporter;

public class AbstractInspectorMojo extends AbstractMojo {
    @Parameter(property = "project", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(property = "plugin.artifactMap", readonly = true, required = true)
    protected Map<String, Artifact> pluginArtifactMap;    

    @Parameter(property = "inspector", defaultValue = "com.github.universetraveller.java.inspect.inspector.MainInspector")
    protected String inspectorName;

    @Parameter(property = "mainClass", defaultValue = "com.github.universetraveller.java.test.invoker.DefaultClassLoaderJUnitCoreTestInvoker")
    protected String mainClass;

    @Parameter(property = "mainArgs")
    protected String mainArgs;

    @Parameter(property = "classPath")
    protected String classPath;

    @Parameter(property = "breakPointLines")
    protected String breakPointLines;

    @Parameter(property = "inspectMethod", defaultValue = "true")
    protected boolean inspectMethod;

    @Parameter(property = "inspectGlobalMethod", defaultValue = "true")
    protected boolean inspectGlobalMethod;

    @Parameter(property = "inspectImplicitMethod", defaultValue = "false")
    protected boolean inspectImplicitMethod;

    @Parameter(property = "inspectSpectra", defaultValue = "true")
    protected boolean inspectSpectra;

    @Parameter(property = "inspectException", defaultValue = "true")
    protected boolean inspectException;

    @Parameter(property = "inspectOutput", defaultValue = "true")
    protected boolean inspectOutput;

    @Parameter(property = "inspectFields", defaultValue = "true")
    protected boolean inspectFields;

    @Parameter(property = "inspectVariables", defaultValue = "true")
    protected boolean inspectVariables;

    @Parameter(property = "inspectVariableChanges", defaultValue = "true")
    protected boolean inspectVariableChanges;

    @Parameter(property = "oneShotBreakPoint", defaultValue = "true")
    protected boolean oneShotBreakPoint;

    @Parameter(property = "deepStep", defaultValue = "true")
    protected boolean deepStep;

    @Parameter(property = "accurateStep", defaultValue = "false")
    protected boolean accurateStep;

    @Parameter(property = "maxFrameCountToInspect", defaultValue = "1")
    protected int maxFrameCountToInspect;

    @Parameter(property = "classFilterPattern", defaultValue = "${project.groupId}.*")
    protected String classFilterPattern;

    // skip classFilter
    // skip classExclusionFilter

    @Parameter(property = "baseDir")
    protected String baseDir;

    @Parameter(property = "methodToInspect")
    protected String methodToInspect;

    @Parameter(property = "reportDir", defaultValue = "./")
    protected String reportDir;

    protected String internalClassPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        checkProperties();
        internalClassPath = getClassPaths();
        if(classPath != null)
            internalClassPath = classPath + ":" + internalClassPath;
        getLog().info("Used classpath: " + internalClassPath);
        try{
            if(this.inspectorName.equals("com.github.universetraveller.java.inspect.inspector.MainInspector") || this.inspectorName.equals("MainInspector"))
                runMainInspector();
            else if(this.inspectorName.equals("com.github.universetraveller.java.inspect.downstream.fl.SimpleOchiaiFaultLocalization") || this.inspectorName.equals("SimpleOchiaiFaultLocalization"))
                runSimpleOchiaiFaultLocalization();
            else
                throw new IllegalStateException("No valid inspector exists");
        }catch(Exception e){
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void runMainInspector() {
        String[] args = new String[0];
        if(this.mainClass.startsWith("com.github.universetraveller.java.test"))
            args = new String[]{this.methodToInspect, this.internalClassPath};
        else
            args = this.mainArgs.split(" ");

        int[] bpl = new int[0];
        if(this.breakPointLines != null){
            String[] sbpl = this.breakPointLines.split(",");
            bpl = new int[sbpl.length];
            for(int i = 0; i < sbpl.length; i++)
                bpl[i] = Integer.parseInt(sbpl[i]);
        }
        Map<String, List<String>> methodMap = new HashMap<>();
        if(this.methodToInspect != null){
            for(String classWithMethod : this.methodToInspect.split("#")){
                String[] pair = classWithMethod.split("::");
                methodMap.putIfAbsent(pair[0], new ArrayList<>());
                methodMap.get(pair[0]).add(pair[1]);
            }
        }
        if(this.baseDir == null){
            if(this.mainClass.startsWith("com.github.universetraveller.java.test"))
                this.baseDir = this.project.getBuild().getTestSourceDirectory();
            else
                this.baseDir = this.project.getBuild().getSourceDirectory();
        }

        MainInspector inspector = MainInspector.getInstance()
                                    .configMainClass(mainClass)
                                    .configMainArgs(args)
                                    .configClassPath(internalClassPath)
                                    .configBreakPointLines(bpl)
                                    .configInspectMethod(inspectMethod)
                                    .configInspectGlobalMethod(inspectGlobalMethod)
                                    .configInspectImplicitMethod(inspectImplicitMethod)
                                    .configInspectSpectra(inspectSpectra)
                                    .configInspectException(inspectException)
                                    .configInspectOutput(inspectOutput)
                                    .configInspectFields(inspectFields)
                                    .configInspectVariables(inspectVariables)
                                    .configInspectVariableChanges(inspectVariableChanges)
                                    .configOneShotBreakPoint(oneShotBreakPoint)
                                    .configDeepStep(deepStep)
                                    .configAccurateStep(accurateStep)
                                    .configMaxFrameCountToInspect(maxFrameCountToInspect)
                                    .configClassFilterPattern(classFilterPattern)
//                                  .configClassFilter()
//                                  .configClassExclusionFilter()
                                    .configBaseSrcDir(baseDir)
                                    .configMethodToInspect(methodMap);
        
        inspector.execute(new DefaultInspectorRunner());
        FileInspectionReporter reporter = new FileInspectionReporter(this.reportDir + "/InspectionReport", inspector);
        reporter.generateReport();
    }

     private void runSimpleOchiaiFaultLocalization()throws IOException {
        SimpleOchiaiFaultLocalization.getInstance()
            .configTestRunner(mainClass)
            .configClassPath(internalClassPath)
            .configGroupPattern(classFilterPattern)
            .configCwd(reportDir)
            .configReportDir(reportDir)
            .execute();
    }

    private String getClassPaths() {
        Set<String> cps = new HashSet<>();
        cps.add(this.project.getBuild().getOutputDirectory());
        cps.add(this.project.getBuild().getTestOutputDirectory());
        Set<Artifact> dependencies = this.project.getArtifacts();
        for(Artifact artifact : dependencies)
            cps.add(artifact.getFile().getAbsolutePath());
        try {
            for (final Object cpElement : this.project.getTestClasspathElements())
                cps.add(new File((String) cpElement).getAbsolutePath());
        } catch (DependencyResolutionRequiredException e) {
            getLog().warn(e);
        }
        for (Object artifact : this.pluginArtifactMap.values()) {
            final Artifact dependency = (Artifact) artifact;
            if (isRelevantDep(dependency)) {
                cps.add(dependency.getFile().getAbsolutePath());
            }
        }
        StringBuffer builder = new StringBuffer();
        for(String cpe : cps)
            builder.append(":").append(cpe);
        return builder.toString();
    }

    private boolean isRelevantDep(Artifact dependency) {
        return dependency.getGroupId().equals("com.github.universetraveller")
                && dependency.getArtifactId().equals("java-inspect");
    }

    private void checkProperties() throws MojoFailureException {
        if(methodToInspect == null && breakPointLines == null)
            throw new MojoFailureException("Should have at least one inspected method or breakpoint");
        
        if(mainClass == null || inspectorName.isEmpty())
            throw new MojoFailureException("mainClass and inspectorName should both be not empty");

        try{
            Class mainClassGot = Class.forName(this.mainClass);
            if(mainClassGot == null)
                throw new ClassNotFoundException("mainClass not found");
        }catch(ClassNotFoundException e){
            throw new MojoFailureException("mainClass not found", e);
        }
    }
}
