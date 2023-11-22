package com.github.universetraveller.java.inspect.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "inspect", requiresDependencyResolution = ResolutionScope.TEST)
public class InspectorMojo extends AbstractInspectorMojo {
    
}
