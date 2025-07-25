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

package de.iip_ecosphere.platform.tools.maven.dependencies;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Reused build-classpath Mojo, allowing to prepend/append classpath elements not part of the Maven classpath.
 * Happens for generated platform services.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "build-classpath", requiresDependencyResolution = ResolutionScope.TEST, 
    defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class BuildClasspathMojo extends org.apache.maven.plugins.dependency.fromDependencies.BuildClasspathMojo {

    private static final Function<String, String> TO_WIN = s -> null == s ? null : s.replace('/', '\\');
    private static final Function<String, String> TO_LINUX = s -> null == s ? null : s.replace('\\', '/');

    @Parameter( property = "mdep.outputFile" )
    private File outputFile; // -> setter
    
    @Parameter( property = "mdep.pathSeparator", defaultValue = "" )
    private String pathSeparator; // -> setter
    
    @Parameter( property = "mdep.prefix" )
    private String prefix; // -> setter
    
    @Parameter( property = "mdep.localRepoProperty", defaultValue = "" )
    private String localRepoProperty; // -> setter

    @Parameter( property = "mdep.cleanup", defaultValue = "true" )
    private boolean cleanup;

    @Parameter(required = false) 
    private List<String> befores;
    
    @Parameter(required = false) 
    private List<String> prepends;

    @Parameter(required = false) 
    private List<String> appends;

    @Parameter(required = false) 
    private List<String> afters;
    
    @Parameter( property = "mdep.lineSeparator", defaultValue = "\n" )
    private String lineSeparator;

    @Parameter(required = false) 
    private boolean rollout;
    
    private Set<String> oldEntries;

    @Override
    public void setOutputFile(File outputFile) {
        super.setOutputFile(outputFile);
        this.outputFile = outputFile;
    }
    
    @Override
    public void setPathSeparator(String thePathSeparator) {
        super.setPathSeparator(thePathSeparator);
        this.pathSeparator = thePathSeparator;
    }
    
    @Override
    public void setLocalRepoProperty(String localRepoProperty) {
        super.setLocalRepoProperty(localRepoProperty);
        this.localRepoProperty = localRepoProperty;
    }
    
    @Override
    public void setPrefix(String thePrefix) {
        super.setPrefix(thePrefix);
        this.prefix = thePrefix;
    }

    /**
     * Sets the prepends.
     * 
     * @param prepends the prepends
     */
    public void setPrepends(List<String> prepends) {
        this.prepends = prepends;
    }

    /**
     * Sets the befores.
     * 
     * @param befores the befores
     */
    public void setBefores(List<String> befores) {
        this.befores = befores;
    }

    /**
     * Returns the classpath contents as a collection of entries/tokens.
     * 
     * @param <C> the type of collection
     * @param contents the classpath contents
     * @param result the result collection instance
     * @return the entries in {@code result}
     */
    static <C extends Collection<String>> C readEntries(String contents, C result) {
        StringTokenizer entries = new StringTokenizer(contents, ":;");
        while (entries.hasMoreTokens()) {
            result.add(entries.nextToken());
        }
        return result;
    }

    /**
     * Returns the classpath contents as a set of entries/tokens.
     * 
     * @param contents the classpath contents
     * @return the entries
     */
    static Set<String> readEntriesToSet(String contents) {
        return readEntries(contents, new HashSet<String>());
    }

    /**
     * Returns the classpath contents as a list of entries/tokens.
     * 
     * @param contents the classpath contents
     * @return the entries
     */
    static List<String> readEntriesToList(String contents) {
        return readEntries(contents, new ArrayList<String>());
    }
    
    /**
     * Copies a given list and applies the specific {@code transform} function to the elements.
     * 
     * @param list the list to copy, may be <b>null</b>
     * @param transform the transform function
     * @return the copied list, <b>null</b> if {@code list} is <b>null</b>
     */
    private static List<String> copy(List<String> list, Function<String, String> transform) {
        List<String> result = list;
        if (result != null) {
            result = new ArrayList<String>(list.size());
            for (String e : list) {
                result.add(transform.apply(e));
            }
        }
        return result;
    }

    @Override
    protected void doExecute() throws MojoExecutionException {
        doExecuteImpl(); // the basic execution
        if (rollout) {
            File initialOutputFile = outputFile;
            String initialPrefix = prefix; // whyever we do not get the values as property
            String initialLocalRepoProperty = localRepoProperty;
            List<String> initialAppends = appends;
            List<String> initialPrepends = prepends;
            
            outputFile = new File(initialOutputFile.toString() + "-win");
            setOutputFile(outputFile);
            setPathSeparator(";");
            setFileSeparator("\\");
            setLocalRepoProperty(TO_WIN.apply(initialLocalRepoProperty));
            setPrefix(TO_WIN.apply(initialPrefix));
            appends = copy(initialAppends, TO_WIN);
            prepends = copy(initialPrepends, TO_WIN);
            lineSeparator = "\r\n";
            doExecuteImpl();

            outputFile = new File(initialOutputFile.toString() + "-linux");
            setOutputFile(outputFile);
            setPathSeparator(":");
            setFileSeparator("/");
            setLocalRepoProperty(TO_LINUX.apply(initialLocalRepoProperty));
            setPrefix(TO_LINUX.apply(initialPrefix));
            appends = copy(initialAppends, TO_LINUX);
            prepends = copy(initialPrepends, TO_LINUX);
            lineSeparator = "\n";
            doExecuteImpl();
        }
    }
    
    /**
     * The actual extended implementation.
     * 
     * @throws MojoExecutionException if the execution fails
     */
    private void doExecuteImpl() throws MojoExecutionException {
        if (cleanup && outputFile.exists()) {
            try {
                oldEntries = readEntriesToSet(FileUtils.readFileToString(outputFile, Charset.defaultCharset()));
            } catch (IOException e) {
                getLog().error("Reading: " + e.getMessage());
            }
        }
        super.doExecute();
        boolean hasPrepends = (prepends != null && !prepends.isEmpty());
        boolean hasAppends = (appends != null && !appends.isEmpty());
        boolean hasBefores = (befores != null && !befores.isEmpty());
        boolean hasAfters = (afters != null && !afters.isEmpty());
        boolean hasAdditionalContents = hasPrepends || hasAppends || hasBefores || hasAfters;
        MavenProject project = getProject();
        String self = project.getGroupId().replace(".", "/") + "/" + project.getArtifactId() + "/" 
               + project.getVersion() + "/" + project.getArtifactId() + "-" + project.getVersion();
        String selfTest = self + "-tests.jar";
        self += ".jar";
        if (hasAdditionalContents && outputFile != null) {
            try {
                String content = FileUtils.readFileToString(outputFile, Charset.defaultCharset());
                if (hasPrepends) {
                    String tmp = "";
                    for (String s : prepends) {
                        s = s.replace("${self}", self);
                        s = s.replace("${self-test}", selfTest);
                        tmp += s + pathSeparator;
                    }
                    content = tmp + content;
                }
                if (hasAppends) {
                    String tmp = "";
                    for (String s : appends) {
                        s = s.replace("${self}", self);
                        s = s.replace("${self-test}", selfTest);
                        tmp += pathSeparator + s;
                    }
                    content = content + tmp;
                }
                if (hasBefores) {
                    content = String.join(lineSeparator, befores) + lineSeparator +  content;
                }
                if (hasAfters) {
                    content += lineSeparator + String.join(lineSeparator, afters);
                }
                FileUtils.write(outputFile, content, Charset.defaultCharset());
                getLog().info("Appended/prepended to: " + outputFile);
            } catch (IOException e) {
                getLog().error("Appending/prepending: " + e.getMessage());
            }
        }
        if (cleanup && outputFile.exists() && oldEntries != null) {
            try {
                Set<String> newEntries = readEntriesToSet(
                    FileUtils.readFileToString(outputFile, Charset.defaultCharset()));
                for (String e : newEntries) {
                    oldEntries.remove(e);
                }
                for (String e : oldEntries) {
                    getLog().info("Cleaning outdated classpath entry " + e);
                    FileUtils.deleteQuietly(new File(e));
                }
            } catch (IOException e) {
                getLog().error("Reading: " + e.getMessage());
            }           
        }
    }

}
