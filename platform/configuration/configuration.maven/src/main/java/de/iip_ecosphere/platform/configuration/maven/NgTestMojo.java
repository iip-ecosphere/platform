/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.iip_ecosphere.platform.tools.maven.python.AbstractLoggingMojo;

/**
 * An Angular build MOJO. Part of this project due to pragmatic reasons.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "ngTest", defaultPhase = LifecyclePhase.TEST)
public class NgTestMojo extends AbstractLoggingMojo {

    @Parameter(property = "configuration.ngTest.skip", required = false, defaultValue = "false")
    private boolean skip;
    
    @Parameter(property = "configuration.ngTest.noWatch", required = false, defaultValue = "true")
    private boolean noWatch;

    @Parameter(property = "configuration.ngTest.noProgress", required = false, defaultValue = "true")
    private boolean noProgress;

    @Parameter(property = "configuration.ngTest.headless", required = false, defaultValue = "true")
    private boolean headless;

    @Parameter(property = "configuration.ngTest.coverage", required = false, defaultValue = "true")
    private boolean coverage;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            ProcessUnit pu = new ProcessUnit.ProcessUnitBuilder("ng test", this)
                .addArgumentOrScriptCommand("ng")
                .addArgument("test")
                .addArgument(noWatch, "--no-watch")
                .addArgument(noProgress, "--no-progress")
                .addArgument(headless, "--browsers=ChromeHeadless")
                .addArgument(coverage, "--code-coverage")
                .redirectErr2In()
                .build();
            int status = pu.waitFor();
            if (ProcessUnit.isFailed(status)) {
                throw new MojoExecutionException(pu.getDescription() + " failed with status: " + status);
            }
            
        }
    }
    
}
