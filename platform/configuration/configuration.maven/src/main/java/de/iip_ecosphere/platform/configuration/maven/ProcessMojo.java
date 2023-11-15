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

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import de.iip_ecosphere.platform.configuration.maven.ProcessUnit.ProcessUnitBuilder;
import de.iip_ecosphere.platform.tools.maven.python.AbstractLoggingMojo;

/**
 * An generic process execution MOJO.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "process", defaultPhase = LifecyclePhase.COMPILE)
public class ProcessMojo extends AbstractLoggingMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(property = "configuration.process.skip", required = false, defaultValue = "false")
    private boolean skip;
    
    @Parameter(property = "configuration.process.processes", required = false)
    private List<BasicProcessSpec> processes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip && null != processes) {
            for (BasicProcessSpec p : processes) {
                ProcessUnitBuilder builder = new ProcessUnitBuilder(p.getDescription(), this);
                builder.addArgumentOrScriptCommand(p.isCmdAsScript(), p.getCmd());
                if (null != p.getHome()) {
                    builder.setHome(p.getHome());
                }
                p.allocatePorts(project, getLog());
                builder.addArguments(p.extrapolateArgs());
                ProcessUnit pu = builder.build4Mvn();
                int status = pu.waitFor();
                if (status != ProcessUnit.UNKOWN_EXIT_STATUS && status != 0) {
                    throw new MojoExecutionException(pu.getDescription() + " terminated with status: " + status);
                }
            }
        }
    }
    
}
