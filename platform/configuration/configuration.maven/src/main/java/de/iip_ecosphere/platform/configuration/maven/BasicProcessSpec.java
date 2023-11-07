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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import de.iip_ecosphere.platform.support.NetUtils;

/**
 * Defines an additional process to be executed in the build process.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicProcessSpec {
    
    @Parameter(required = true)
    private String description;

    @Parameter(required = true)
    private String cmd;

    @Parameter(required = false, defaultValue = "false")
    private boolean cmdAsScript;

    @Parameter(required = false)
    private List<String> args;
    
    @Parameter(required = false, defaultValue = "")
    private File home;
    
    @Parameter(required = false)
    private List<PortSpec> ports;
    
    /**
     * Port allocation/mapping specification.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class PortSpec {
        
        @Parameter(required = false, defaultValue = "-1")
        private int port;

        @Parameter(required = false, defaultValue = "")
        private String property;

        private int allocatedPort;
        
    }

    /**
     * Returns the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description. [mvn]
     * 
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the command.
     * 
     * @return the cmd
     */
    public String getCmd() {
        return cmd;
    }

    /**
     * Defines the command. [mvn]
     * 
     * @param cmd the cmd to set
     */
    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    /**
     * Returns whether {@link #getCmd()} shall be treated like a script.
     * 
     * @return the cmdAsScript
     */
    public boolean isCmdAsScript() {
        return cmdAsScript;
    }

    /**
     * Sets that {@link #getCmd() cmd} shell be treated like a script. [mvn]
     * 
     * @param cmdAsScript the cmdAsScript to set
     */
    public void setCmdAsScript(boolean cmdAsScript) {
        this.cmdAsScript = cmdAsScript;
    }

    /**
     * Returns the command line arguments for {@link #getCmd() cmd}.
     * 
     * @return the args
     */
    public List<String> getArgs() {
        return args;
    }

    /**
     * Defines the command line arguments for {@link #getCmd() cmd}. [mvn]
     * 
     * @param args the args to set
     */
    public void setArgs(List<String> args) {
        this.args = args;
    }

    /**
     * Returns the process home directory.
     * 
     * @return the home directory
     */
    public File getHome() {
        return home;
    }

    /**
     * Defines the process home directory. [mvn]
     * 
     * @param home the home directory to set
     */
    public void setHome(File home) {
        this.home = home;
    }

    /**
     * Allocates the ports.
     * 
     * @param project the maven project to be modified
     * @param log the log
     */
    void allocatePorts(MavenProject project, Log log) {
        if (null != ports) {
            for (PortSpec p : ports) {
                p.allocatedPort = p.port;
                if (p.allocatedPort <= 0) {
                    p.allocatedPort = NetUtils.getEphemeralPort();
                }
                if (p.property != null && p.property.length() > 0) {
                    project.getProperties().setProperty(p.property, String.valueOf(p.allocatedPort));
                    log.info("Assigned " + p.property + "=" + p.allocatedPort);
                } else {
                    log.info("Allocated port " + p.allocatedPort);
                }
            }
        }
    }

    /**
     * Extrapolates the property expressions.
     * 
     * @return the extrapolated arguments
     */
    List<String> extrapolateArgs() {
        return extrapolateArgs(args);
    }

    /**
     * Extrapolates the property expressions.
     * 
     * @param args the arguments to interpret the expressions for
     * @return the extrapolated arguments
     */
    List<String> extrapolateArgs(List<String> args) {
        List<String> result = null;
        if (null != args) {
            result = new ArrayList<>();
            for (String a: args) {
                if (null != ports) {
                    for (PortSpec p : ports) {
                        if (p.property != null && p.property.length() > 0) {
                            a = a.replace("${" + p.property + "}", String.valueOf(p.allocatedPort));
                        }
                    }
                }
                result.add(a);
            }
        }
        return result;
    }

}
