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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Reused build-classpath Mojo, allowing to prepend/append classpath elements not part of the Maven classpath.
 * Happens for generated platform services.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "build-classpath", requiresDependencyResolution = ResolutionScope.TEST, 
    defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class BuildClasspathMojo extends org.apache.maven.plugins.dependency.fromDependencies.BuildClasspathMojo {

    @Parameter( property = "mdep.outputFile" )
    private File outputFile;
    
    @Parameter( property = "mdep.pathSeparator", defaultValue = "" )
    private String pathSeparator;

    @Parameter( property = "mdep.cleanup", defaultValue = "true" )
    private boolean cleanup;

    @Parameter(required = false) 
    private List<String> prepends;

    @Parameter(required = false) 
    private List<String> appends;
    
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

    @Override
    protected void doExecute() throws MojoExecutionException {
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
        if ((hasPrepends || hasAppends) && outputFile != null) {
            try {
                String content = FileUtils.readFileToString(outputFile, Charset.defaultCharset());
                if (hasPrepends) {
                    String tmp = "";
                    for (String s : prepends) {
                        tmp += s + pathSeparator;
                    }
                    content = tmp + content;
                }
                if (hasAppends) {
                    String tmp = "";
                    for (String s : prepends) {
                        tmp += pathSeparator + s;
                    }
                    content = content + tmp;
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
