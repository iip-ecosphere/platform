/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * If the service is not completely implemented rather than delegates functionality to an additional process that
 * must be started and managed along with the service.
 *  
 * @author Holger Eichelberger, SSE
 */
public class YamlProcess {

    private String executable;
    private File executablePath;
    private File home;
    private List<String> cmdArg = new ArrayList<>();
    
    /**
     * Returns the system command or relative path to be executed.
     * 
     * @return the command or relative path
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Returns an optional path to be prefixed before the executable. Relevance depends on the execution environment.
     * 
     * @return the optional executable path, may be <b>null</b> for none
     */
    public File getExecutablePath() {
        return executablePath;
    }

    /**
     * Returns the home directory of the process to be executed.
     * 
     * @return the home directory, may be <b>null</b> to rely on extracted paths, may be given to explicitly 
     *     define a home path
     */
    public File getHome() {
        return home;
    }
    
    /**
     * Returns the command line arguments to start the process. 
     * 
     * @return the command line arguments (may be empty for none)
     */
    public List<String> getCmdArg() {
        return cmdArg;
    }

    /**
     * Defines the system command or relative path to be executed. [required by SnakeYaml]
     * 
     * @param executable the name/path
     */
    public void setExecutable(String executable) {
        this.executable = executable;
    }
    
    /**
     * Changes the optional path to be prefixed before the executable. Relevance depends on the execution environment. 
     * [required by SnakeYaml]
     * 
     * @param executablePath the optional executable path, may be <b>null</b> for none
     */
    public void setExecutablePath(File executablePath) {
        this.executablePath = executablePath;
    }
    
    /**
     * Changes the home directory of the process to be executed. [required by SnakeYaml]
     * 
     * @param home the home directory, may be <b>null</b> to rely on extracted paths, may be given to explicitly 
     *     define a home path
     */
    public void setHome(File home) {
        this.home = home;
    }

    /**
     * Defines the command line arguments. [required by SnakeYaml]
     * 
     * @param cmdArg the command line arguments (may be empty for none)
     */
    public void setCmdArg(List<String> cmdArg) {
        this.cmdArg = cmdArg;
    }

}
