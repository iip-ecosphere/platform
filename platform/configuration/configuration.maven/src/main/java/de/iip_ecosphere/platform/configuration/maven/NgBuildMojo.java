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

/**
 * An Angular build MOJO. Part of this project due to pragmatic reasons.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "ngBuild", defaultPhase = LifecyclePhase.COMPILE)
public class NgBuildMojo extends AbstractLoggingMojo {

    @Parameter(property = "configuration.ngBuild.skip", required = false, defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            ProcessUnit pu = new ProcessUnit.ProcessUnitBuilder("npm install", this)
                .addArgument("npm")
                .addArgument("install")
                .build();
            int status = pu.waitFor();
            if (status != 0) {
                throw new MojoExecutionException(pu.getDescription() + " failed with status: " + status);
            }
            pu = new ProcessUnit.ProcessUnitBuilder("ng build", this)
                .addShellScriptCommand("ng")
                .addArgument("build")
                .build();
            status = pu.waitFor();
            if (status != 0) {
                throw new MojoExecutionException(pu.getDescription() + " failed with status: " + status);
            }
        }
    }
    
}
