/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * A simple "touch" command.
 * 
 * @author Gemini
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "touch", threadSafe = true, defaultPhase = LifecyclePhase.DEPLOY )
public class TouchMojo extends AbstractMojo {

    @Parameter( property = "touch.file", required = true )
    private File file;

    @Parameter( property = "updateArchive.deleteBefore", required = false, defaultValue = "false" )
    private boolean deleteBefore;

    @Parameter( property = "updateArchive.skip", required = false, defaultValue = "false" )
    private boolean skip;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping execution.");
        } else {
            try {
                if (deleteBefore) {
                    FileUtils.deleteQuietly(file);
                }
                FileUtils.touch(file);
            } catch (IOException e) {
                throw new MojoExecutionException("Touching " + file + ": " + e);
            }
        }
    }

}
