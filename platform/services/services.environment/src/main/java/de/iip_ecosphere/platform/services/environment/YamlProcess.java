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

import org.apache.commons.io.FileUtils;

/**
 * If the service is not completely implemented rather than delegates functionality to an additional process that
 * must be started and managed along with the service.
 *  
 * @author Holger Eichelberger, SSE
 */
public class YamlProcess implements ProcessSpec {

    private String executable;
    private String executablePath;
    private String homePath;
    private String locationKey;
    private List<String> execArg = new ArrayList<>();
    private List<String> cmdArg = new ArrayList<>();
    private List<String> artifacts = new ArrayList<String>();
    private boolean started = false;

    @Override
    public List<String> getArtifacts() {
        return artifacts;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    /**
     * Defines the process implementing artifacts within the containing artifact to be extracted.
     * 
     * @param artifacts the relative paths to the artifacts
     */
    public void setArtifacts(List<String> artifacts) {
        this.artifacts = artifacts;
    }

    /**
     * Changes whether the underlying process is already started when firing up the service. [required by SnakeYaml] 
     * 
     * @param started {@code true} for started (default), {@code false} else
     */
    public void setStarted(boolean started) {
        this.started = started;
    }

    @Override
    public String getExecutable() {
        return executable;
    }

    /**
     * Returns the location key for lookup in {@link InstalledDependenciesSetup}.
     * 
     * @return the location key, may be <b>null</b>
     */
    public String getLocationKey() {
        return locationKey;
    }

    @Override
    public File getExecutablePath() {
        return toSubstFilePath(executablePath);
    }

    @Override
    public File getHomePath() {
        return toSubstFilePath(homePath);
    }

    /**
     * Substitutes "${tmp}" and "${user}" and returns a name for {@code path}.
     * 
     * @param path the path
     * @return the name, may be <b>null</b> if {@code path} is <b>null</b> or empty
     */
    protected static String toSubstFileName(String path) {
        String result;
        if (null == path || path.length() == 0) {
            result = null;
        } else {
            result = path.replace("${tmp}", FileUtils.getTempDirectoryPath());
            result = result.replace("${user}", FileUtils.getUserDirectoryPath());
            result = new File(result).toString(); // prevent OS file sep mismatch
        }
        return result;
    }

    /**
     * Substitutes "${tmp}" and "${user}" and returns a file for {@code path}.
     * 
     * @param path the path
     * @return the file, may be <b>null</b> if {@code path} is <b>null</b> or empty
     */
    protected static File toSubstFilePath(String path) {
        String tmp = toSubstFileName(path);
        return null == tmp ? null : new File(tmp);
    }
    
    @Override
    public List<String> getCmdArg() {
        return cmdArg;
    }
    
    @Override
    public List<String> getExecArg() {
        return execArg;
    }
    
    /**
     * Returns {@link #getCmdArg()} with substitutions from {@link #toSubstFileName(String)}
     * for all arguments.
     * 
     * @return the substitutions
     */
    public List<String> getSubstCmdArg() {
        List<String> result = new ArrayList<>();
        for (String s : cmdArg) {
            result.add(toSubstFileName(s));
        }
        return result;
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
     * Changes the location key for lookup in {@link InstalledDependenciesSetup}.
     * 
     * @param locationKey the location key, may be <b>null</b>
     */
    public void setLocationKey(String locationKey) {
        this.locationKey = locationKey;
    }
    
    /**
     * Changes the optional path to be prefixed before the executable. Relevance depends on the execution environment. 
     * May contain "${tmp}" for the system temporary directory path and "${user}" for the user directory path.
     * [required by SnakeYaml]
     * 
     * @param executablePath the optional executable path, may be <b>null</b> for none
     */
    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    /**
     * Changes the optional path to be prefixed before the executable. Relevance depends on the execution environment. 
     * May contain "${tmp}" for the system temporary directory path and "${user}" for the user directory path.
     * [required by SnakeYaml]
     * 
     * @param executablePath the optional executable path, may be <b>null</b> for none
     */
    public void setExecutablePath(File executablePath) {
        this.executablePath = null == executablePath ? null : executablePath.toString();
    }
    
    /**
     * Changes the home directory of the process to be executed. [required by SnakeYaml]
     * May contain "${tmp}" for the system temporary directory path and "${user}" for the user directory path.
     * 
     * @param homePath the home directory, may be <b>null</b> to rely on extracted paths, may be given to explicitly 
     *     define a home path
     */
    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }
    
    /**
     * Changes the home directory of the process to be executed. [required by SnakeYaml]
     * May contain "${tmp}" for the system temporary directory path and "${user}" for the user directory path.
     * 
     * @param home the home directory, may be <b>null</b> to rely on extracted paths, may be given to explicitly 
     *     define a home path
     */
    public void setHomePath(File home) {
        this.homePath = null == home ? null : home.toString();
    }
    
    /**
     * Defines the arguments to be passed to the executable itself. [required by SnakeYaml]
     * Executable args and command line args may be the same in many cases, but also may differ for Java (-D arguments)
     * or Python/Conda (conda arguments).
     * 
     * @param execArg the command line executable arguments (may be empty for none)
     */
    public void setExecArg(List<String> execArg) {
        this.execArg = execArg;
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
