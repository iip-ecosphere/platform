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
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.dependency.fromConfiguration.ArtifactItem;
import org.apache.maven.plugins.dependency.fromConfiguration.UnpackMojo;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * Extended unpack Mojo.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "unpack", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresProject = false, threadSafe = true)
public class CleaningUnpackMojo extends UnpackMojo {

    /**
     * A specific <code>fileSet</code> rule to select files and directories.
     */
    @Parameter(required = false)
    private FileSet cleanup;
    
    @Parameter(property = "unpack.initiallyAllowed", required = false, defaultValue = "")
    private String initiallyAllowed;

    @Parameter(property = "unpack.initiallyAllowedFile", required = false, defaultValue = "")
    private File initiallyAllowedFile;

    @Parameter(property = "unpack.force", required = false, defaultValue = "false")
    private boolean force;

    /**
     * Determines excluded paths from the given paths, i.e., split the directories add them to {@code excluded}.
     * 
     * @param paths the paths to be excluded
     * @param isFile whether path represents a file (than ignore the last path) or whether it is a folder
     * @param excluded the excluded paths to be modified as a side effect
     */
    private void addExcludedPaths(String[] paths, boolean isFile, Set<String> excluded) {
        for (String p : paths) {
            addExcludedPaths(p, isFile, excluded);
        }
    }

    /**
     * Determines excluded paths from the given path, i.e., split the directories add them to {@code excluded}.
     * 
     * @param path the path to be excluded
     * @param isFile whether path represents a file (than ignore the last path) or whether it is a folder
     * @param excluded the excluded paths to be modified as a side effect
     */
    private void addExcludedPaths(String path, boolean isFile, Set<String> excluded) {
        excluded.add(path);
        String[] subPaths = path.replace("\\", "/").split("/");
        if (subPaths.length > 0) {
            String tmp = "";
            for (int i = 0; i < subPaths.length - (isFile ? 1 : 0); i++) { // if isFile, not the file name
                if (i > 0) {
                    tmp += "/";
                }
                tmp += subPaths[i];
                excluded.add(tmp);
            }
        }
    }
    
    /**
     * Deletes the given paths.
     * 
     * @param paths the paths to be deleted
     * @param excluded paths that shall not be deleted (excluded)
     */
    private void deletePaths(String[] paths, Set<String> excluded) {
        for (String p : paths) {
            if (!excluded.contains(p)) {
                File file = new File(cleanup.getDirectory(), p);
                getLog().info("Deleting " + file);
                FileUtils.deleteQuietly(file);
            }
        }
    }

    /**
     * Returns whether there is a setup for initially allowed files, considering {@link #initiallyAllowedFile} and 
     * {@link #initiallyAllowed}.
     *  
     * @return {@code true} if there is some setup, {@code false} else
     */
    private boolean hasInitiallyAllowed() {
        return (initiallyAllowed != null || initiallyAllowedFile != null);
    }
    
    /**
     * Returns the initially allowed files, considering {@link #initiallyAllowedFile} and {@link #initiallyAllowed}.
     *  
     * @return the initially allowed files
     */
    private Set<String> getInitiallyAllowed() {
        Set<String> allowed = new HashSet<String>();
        if (null != initiallyAllowedFile) {
            try {
                List<String> allLines = Files.readAllLines(initiallyAllowedFile.toPath());
                allowed.addAll(allLines);
                allowed.add(initiallyAllowed.toString());
                getLog().info("Taking initially allowed files from " + initiallyAllowedFile);
            } catch (IOException e) {
                getLog().warn("Cannot read initially allowed files from " + initiallyAllowedFile 
                    + ": " + e.getMessage());
            }
        }
        if (null != initiallyAllowed) {
            String tmp = initiallyAllowed.replace(";", ":");
            getLog().info("Taking initially allowed files from POM " + initiallyAllowed);
            Collections.addAll(allowed, tmp.split(":"));
        }
        return allowed;
    }
    
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        boolean execute;
        if (force) {
            execute = true;
        } else {
            execute = false;
            for (ArtifactItem ai : getArtifactItems()) {
                boolean outDirExists = ai.getOutputDirectory().exists();
                execute |= ai.isNeedsProcessing() || !outDirExists;
                if (!execute && outDirExists && hasInitiallyAllowed()) {
                    Set<String> allowed = getInitiallyAllowed();
                    getLog().info("Output directory " + ai.getOutputDirectory() + " exists. "
                        + "Checking for initially allowed files: " + allowed);
                    execute = true;
                    for (String fn : ai.getOutputDirectory().list()) {
                        if (!allowed.contains(fn)) {
                            getLog().info("Disabling execution as " + fn + " is not initially allowed");
                            execute = false;
                        }
                    }
                }
            }
        }        
        
        if (execute) {
            if (null != cleanup) {
                FileSetManager fileSetManager = new FileSetManager();
                Set<String> excluded = new HashSet<>();
                excluded.add("");  // don't delete containing directory, it's part of included directories
                excluded.add(".");
    
                addExcludedPaths(fileSetManager.getExcludedDirectories(cleanup), false, excluded);
                addExcludedPaths(fileSetManager.getExcludedFiles(cleanup), true, excluded);
    
                deletePaths(fileSetManager.getIncludedFiles(cleanup), excluded);
                deletePaths(fileSetManager.getIncludedDirectories(cleanup), excluded);
            }
            super.doExecute();
        }
    }

}
