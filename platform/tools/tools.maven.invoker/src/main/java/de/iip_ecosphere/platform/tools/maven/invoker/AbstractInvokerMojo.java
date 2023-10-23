/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.tools.maven.invoker;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.CommandLineConfigurationException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenCommandLineBuilder;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Maven POM invoker plugin. This plugin is largely inspired by the maven-invoker plugin.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractInvokerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(required = true)
    private List<String> invokeGoals;

    @Parameter
    private List<String> invokeProfiles;

    @Parameter
    private List<SystemProperty> systemProperties;

    /**
     * The local repository for caching artifacts.
     */
    @Parameter(defaultValue = "${settings.localRepository}" )
    private File localRepositoryPath;

    /**
     * Flag to enable show mvn version used for running its (cli option : -V,--show-version ).
     */
    @Parameter(defaultValue = "false" )
    private boolean showVersion;

    /**
     * Whether to show errors in the build output.
     */
    @Parameter(defaultValue = "true" )
    private boolean showErrors;

    /**
     * The <code>JAVA_HOME</code> environment variable to use for forked Maven invocations. Defaults to the current Java
     * home directory.
     */
    @Parameter
    private File javaHome;

    /**
     * The home directory of the Maven installation to use for the forked builds. Defaults to the current Maven
     * installation.
     */
    @Parameter
    private File mavenHome;

    /**
     * mavenExecutable can either be a file relative to <code>${maven.home}/bin/</code>, test project workspace
     * or an absolute file.
     */
    @Parameter
    private File mavenExecutable;

    @Parameter(defaultValue = "0")
    private int timeoutInSeconds;
    
    @Parameter
    private File pom;
    
    @Parameter(defaultValue = "${session.offline}")
    private boolean offline;

    @Parameter(defaultValue = "${session.request}")
    private MavenExecutionRequest execRequest;

    @Parameter(property = "unpack.force", required = false, defaultValue = "false") 
    private boolean unpackForce;
    
    @Parameter(property = "disableJava", defaultValue = "false") 
    private boolean disableJava;

    @Parameter(property = "disablePython", defaultValue = "false") 
    private boolean disablePython;

    @Parameter(property = "disablePythonTests", defaultValue = "false") 
    private boolean disablePythonTests;

    @Parameter(property = "disableBuild", defaultValue = "false") 
    private boolean disableBuild;

    @Parameter(property = "python-compile.hashDir", defaultValue = "") 
    private String pythonCompileHashDir;

    @Parameter(property = "enableJavadoc", defaultValue = "false") 
    private boolean enableJavadoc;
    
    @Component
    private Invoker invoker;
    
    private boolean enabled = true;
    
    /**
     * Disables the execution.
     */
    public void disable() {
        enabled = false;
    }
    
    /**
     * Creates the actual invocation request.
     * 
     * @return the request
     */
    private InvocationRequest createInvocationRequest() {
        final InvocationRequest request = new DefaultInvocationRequest();
        request.setBatchMode(true);
        request.setLocalRepositoryDirectory(localRepositoryPath);
        request.setShowErrors(showErrors);
        request.setShowVersion(showVersion);
        request.setJavaHome(javaHome);
        request.setMavenHome(mavenHome);
        Properties sysProperties = new Properties();
        if (null != systemProperties) {
            for (SystemProperty prop : systemProperties) {
                if (null != prop.getKey() && null != prop.getValue()) {
                    sysProperties.put(prop.getKey(), prop.getValue());
                } else {
                    getLog().error("Skipping property " + prop.getKey() + "=" + prop.getValue() 
                        + " as either key or value are not given");
                }
            }
        }
        if (!enableJavadoc) {
            request.addShellEnvironment("MAVEN_ARGS", "-Dmaven.javadoc.skip=true"); // pass on 2 mvn levels
        }
        if (unpackForce && !sysProperties.containsKey("unpack.force")) {
            sysProperties.put("unpack.force", "true");
        }
        if (disableJava || disableBuild) {
            sysProperties.put("maven.main.skip", "true");
            sysProperties.put("maven.test.skip", "true");
            sysProperties.put("skipTests", "true"); // maven.test.skip might be sufficient
            sysProperties.put("maven.javadoc.skip", "true");
        }
        if (disablePython || disableBuild) {
            sysProperties.put("python-compile.skip", "true");
            sysProperties.put("python-test.skip", "true");
        }
        if (disablePythonTests) {
            sysProperties.put("python-test.skip", "true");
        }
        String hashDir = pythonCompileHashDir;
        if (null == hashDir || hashDir.length() == 0) {
            hashDir = project.getBuild().getDirectory();
        }
        sysProperties.put("python-compile.hashDir", hashDir);
        request.addShellEnvironment("PYTHON_COMPILE_HASHDIR", hashDir); // invoker -D not correct?, pass on 2 mvn levels
        String settings = System.getenv("MAVEN_SETTINGS_PATH");
        if (null == settings) {
            request.setGlobalSettingsFile(execRequest.getGlobalSettingsFile());
            request.setUserSettingsFile(execRequest.getUserSettingsFile());
            File tmp = execRequest.getUserSettingsFile();
            if (null == settings) {
                tmp = execRequest.getGlobalSettingsFile();
            }
            if (null != tmp) {
                settings = tmp.getAbsolutePath();
            }
        } else {
            request.setUserSettingsFile(new File(settings));
        }
        if (null != settings) {
            request.addShellEnvironment("MAVEN_SETTINGS_PATH", settings);
            getLog().info("Passing on env setting MAVEN_SETTINGS_PATH=" + settings);
        }
        request.setProperties(sysProperties);
        File pomFile = pom;
        if (null == pomFile) {
            pomFile = project.getFile();
        }
        request.setBaseDirectory(pomFile.getParentFile());
        request.setPomFile(pomFile);
        request.setGoals(invokeGoals);
        request.setProfiles(invokeProfiles);
        request.setOffline(offline);
        request.setMavenExecutable(mavenExecutable);
        request.setTimeoutInSeconds(timeoutInSeconds);
        
        return request;
    }
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final InvocationRequest request = createInvocationRequest();
        if (enabled) {
            try {
                getLog().info(">>> Maven invoker: Using MAVEN_OPTS: " + request.getMavenOpts());
                getLog().info(">>> Executing: " + new MavenCommandLineBuilder().build(request));
            } catch (CommandLineConfigurationException ex) {
                getLog().debug("Failed to display command line: " + ex.getMessage());
            }

            try {
                InvocationResult result = invoker.execute(request);
                if (result.getExecutionException() != null) {
                    throw new MojoExecutionException( "The Maven invocation failed. "
                        + result.getExecutionException().getMessage());
                } else if (result.getExitCode() != 0) {
                    throw new MojoExecutionException( "The Maven invocation failed. Exit code: " 
                        + result.getExitCode());
                }
            } catch (MavenInvocationException ex) {
                getLog().debug("Error invoking Maven: " + ex.getMessage(), ex);
                throw new MojoExecutionException( "Maven invocation failed. " + ex.getMessage());
            }
            getLog().info("<<< Maven invoker completed");
        } else {
            getLog().info("Maven invoker disabled, not executing.");
        }
    }
    
}
