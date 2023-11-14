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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.dependency.fromConfiguration.ArtifactItem;
import org.apache.maven.plugins.dependency.fromConfiguration.UnpackMojo;
import org.apache.maven.shared.model.fileset.FileSet;

import de.iip_ecosphere.platform.tools.maven.python.FilesetUtils;

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
     * Returns whether there is a setup for initially allowed files, considering {@link #initiallyAllowedFile} and 
     * {@link #initiallyAllowed}.
     *  
     * @return {@code true} if there is some setup, {@code false} else
     */
    private boolean hasInitiallyAllowed() {
        return (initiallyAllowed != null || initiallyAllowedFile != null);
    }
    
    /**
     * Returns the initially allowed files/wildcards. [public/static for testing]
     * 
     * @param initiallyAllowed colon or semicolon separated list of filenames/wildcards, may be <b>null</b>
     * @param initiallyAllowedFile file with line separated list of filenames/wildcards, may be <b>null</b>
     * @param log maven plugin logging instance
     *  
     * @return the initially allowed files
     */
    public static Set<String> getInitiallyAllowed(String initiallyAllowed, File initiallyAllowedFile, Log log) {
        Set<String> allowed = new HashSet<String>();
        if (null != initiallyAllowedFile) {
            try {
                List<String> allLines = Files.readAllLines(initiallyAllowedFile.toPath());
                allowed.addAll(allLines);
                allowed.add(initiallyAllowedFile.toString());
                log.info("Taking initially allowed files from " + initiallyAllowedFile);
            } catch (IOException e) {
                log.warn("Cannot read initially allowed files from " + initiallyAllowedFile 
                    + ": " + e.getMessage());
            }
        }
        if (null != initiallyAllowed) {
            String tmp = initiallyAllowed.replace(";", ":");
            log.info("Taking initially allowed files from POM " + initiallyAllowed);
            Collections.addAll(allowed, tmp.split(":"));
        }
        
        // normalize to enable path matching between windows/linux
        Set<String> tmp = new HashSet<>();
        for (String a : allowed) {
            tmp.add(FilenameUtils.normalize(a));
        }
        return tmp;
    }

    /**
     * Returns whether {@code file} matches at least one of the file names/wildcards in {@code allowed}.
     * 
     * @param file the file to match (including path)
     * @param allowed the allowed file names/wildcards
     * @return {@code true} for match, {@code false} for no match
     */
    public static boolean matches(File file, Collection<String> allowed) {
        for (String a : allowed) {
            if (FilenameUtils.wildcardMatch(FilenameUtils.normalize(file.toString()), a, IOCase.SENSITIVE)) {
                return true;
            }
        }
        return false;
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
                    Set<String> allowed = getInitiallyAllowed(initiallyAllowed, initiallyAllowedFile, getLog());
                    getLog().info("Output directory " + ai.getOutputDirectory() + " exists. "
                        + "Checking for initially allowed files: " + allowed);
                    execute = true;
                    for (File f : ai.getOutputDirectory().listFiles()) {
                        if (matches(f, allowed)) {
                            getLog().info("Disabling execution as " + f + " is not initially allowed");
                            execute = false;
                        }
                    }
                }
            }
        }        
        
        if (execute) {
            FilesetUtils.deletePaths(cleanup, getLog());
            super.doExecute();
        }
    }

}
