/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * A mojo that splits classpaths into main and app classpaths based on contained file name parts. Handlers spring
 * app package as well as oktoflow classpath jars.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "split-classpath", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true )
public class SplitClasspathMojo extends AbstractMojo {

    @Parameter( property = "mdep.archiveFile", defaultValue = "", required = true )
    private List<File> archiveFiles;
    
    @Parameter( required = false )
    private List<String> mainPatterns;

    /**
     * Returns whether the given name/line contains on of the main file patterns.
     * 
     * @param name the name to check
     * @return {@code true} for match, {@code false} else
     */
    private boolean isMainFile(String name) {
        boolean found = false;
        for (String m : mainPatterns) {
            if (name.contains(m)) {
                found = true;
                break;
            }
        }
        return found;
    }
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (mainPatterns == null) {
            mainPatterns = new ArrayList<>();
        }
        if (mainPatterns.isEmpty()) {
            Collections.addAll(mainPatterns, "transport-", "support-", "support.aas-", "support.iip-aas-", 
                "connectors-", "services.environment-");
        }
        for (File f: archiveFiles) {
            processFile(f);
        }
    }

    /**
     * Processes a given file.
     * 
     * @param file the file to be processed
     * @throws MojoExecutionException if processing fails
     */
    private void processFile(File file) throws MojoExecutionException {
        try (FileSystem fs = FileSystems.newFileSystem(file.toPath())) {
            Path mainPath;
            Path appPath = null;
            StringBuilder main = new StringBuilder();
            StringBuilder app = new StringBuilder();

            mainPath = fs.getPath("/classpath");
            if (Files.exists(mainPath)) {
                getLog().info("Processing " + file + " as classpath JAR archive");
                appPath = fs.getPath("/classpath-app");
                List<String> lines = IOUtils.readLines(Files.newInputStream(mainPath), Charset.defaultCharset());
                for (String line: lines) {
                    if (line.startsWith("#")) {
                        main.append(line);
                        main.append("\n");
                        app.append(line);
                        app.append("\n");
                    } else {
                        String sep = ":";
                        StringTokenizer tokenizer = new StringTokenizer(line, sep);
                        boolean firstMain = true;
                        boolean firstApp = true;
                        while (tokenizer.hasMoreTokens()) {
                            String token = tokenizer.nextToken();
                            StringBuilder target;
                            boolean sepBefore;
                            if (isMainFile(token)) {
                                target = main;
                                sepBefore = !firstMain;
                                firstMain = false;
                            } else {
                                target = app;
                                sepBefore = !firstApp;
                                firstApp = false;
                            }
                            if (sepBefore) {
                                target.append(sep);
                            }
                            target.append(token);
                        }
                        main.append("\n");
                        app.append("\n");
                    }
                }
            } else {
                mainPath = fs.getPath("/BOOT-INF/classpath.idx");
                if (Files.exists(mainPath)) {
                    getLog().info("Processing " + file + " as spring app JAR archive");
                    appPath = fs.getPath("/BOOT-INF/classpath-app.idx");
                    List<String> lines = IOUtils.readLines(Files.newInputStream(mainPath), Charset.defaultCharset());
                    for (String line: lines) {
                        StringBuilder target;
                        if (isMainFile(line)) {
                            target = main;
                        } else {
                            target = app;
                        }
                        target.append(line);
                        target.append("\n");
                    }
                }
            }
            if (null != appPath) {
                Files.writeString(mainPath, main.toString());
                Files.writeString(appPath, app.toString());
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

}
