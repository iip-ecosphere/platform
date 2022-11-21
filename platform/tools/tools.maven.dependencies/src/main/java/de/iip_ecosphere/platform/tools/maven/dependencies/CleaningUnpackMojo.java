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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
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
    
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        if (null != cleanup) {
            FileSetManager fileSetManager = new FileSetManager();
            String[] includedFiles = fileSetManager.getIncludedFiles(cleanup);
            for (String f : includedFiles) {
                File file = new File(cleanup.getDirectory(), f);
                getLog().info("Deleting " + file);
                FileUtils.deleteQuietly(file);
            }
            String[] includedDirs = fileSetManager.getIncludedDirectories(cleanup);
            for (String f : includedDirs) {
                File file = new File(cleanup.getDirectory(), f);
                getLog().info("Deleting " + file);
                FileUtils.deleteQuietly(file);
            }
        }
        super.doExecute();
    }

}
