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
import java.util.List;

/**
 * If the service is not completely implemented rather than delegates functionality to an additional process that
 * must be started and managed along with the service. The process implementation (whatever it is) will be extracted 
 * from {@link #getHomePath()}. For the execution in a shell, the home directory will be set to the folder where the 
 * files in {@link #getHomePath()} are located. {@link #getHomePath()} must not be empty, {@link #getCmdArg()} may be 
 * empty.
 *  
 * @author Holger Eichelberger, SSE
 */
public interface ProcessSpec {

    /**
     * Returns the process implementing artifacts within the containing artifact to be extracted into the 
     * {@link #getHomePath() process home directory}.
     * 
     * @return the relative paths to the artifacts, shall start with "/" as part of ZIP/JAR
     */
    public List<String> getArtifacts();

    /**
     * Returns the system command or relative path within the artifact to be executed.
     * 
     * @return the command or relative path
     */
    public String getExecutable();
    
    /**
     * Returns an optional path to be prefixed before the executable. Relevance depends on the execution environment.
     * 
     * @return the optional executable path, may be <b>null</b> for none
     */
    public File getExecutablePath();
    
    /**
     * Returns the home directory of the process to be executed.
     * 
     * @return the home directory, may be <b>null</b> to rely on extracted paths, may be given to explicitly 
     *     define a home path
     */
    public File getHomePath();
    
    /**
     * Returns the command line arguments to start the process. The shell will be executed within the folder where
     * the files from {@link #getHomePath()} are extracted.
     * 
     * @return the command line arguments (may be empty for none)
     */
    public List<String> getCmdArg();
    
    
    /**
     * Returns the arguments to be passed to the executable itself.
     * Executable args and command line args may be the same in many cases, but also may differ for Java (-D arguments)
     * or Python/Conda (conda arguments).
     * 
     * @return the command line executable arguments (may be empty for none)
     */
    public List<String> getExecArg();
    
    /**
     * Returns whether the underlying process is already started when firing up the service or it will be started 
     * through the service implementation. If specified, {@link #getArtifacts() artifacts} will be extracted anyway
     * into the {@link #getHomePath() process home directory}, assuming that a pre-installed executable will not specify
     * artifacts to be extracted.
     * 
     * @return {@code true} for started, {@code false} else (default)
     */
    public boolean isStarted();
    
}
