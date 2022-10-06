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

package de.iip_ecosphere.platform.deviceMgt.storage;

import java.io.File;

/**
 * Setup for a server instance to be created on platform start.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StorageServerSetup {
    
    private int port = -1;
    private File path;
    private String authenticationKey;
    
    /**
     * Get the server port.
     *
     * @return the server port (negative for no server)
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the server port. [required by SnakeYaml]
     *
     * @param port the port (negative for no server)
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * Get the local storage path.
     *
     * @return the path (<b>null</b> or empty for in-memory)
     */
    public File getPath() {
        return path;
    }

    /**
     * Set the local storage path. [required by SnakeYaml]
     *
     * @param path the path (<b>null</b> or empty for in-memory)
     */
    public void setPath(File path) {
        this.path = path;
    }

    /**
     * Get the authentication key pointing to the identity store.
     *
     * @return the authentication key
     */
    public String getAuthenticationKey() {
        return authenticationKey;
    }

    /**
     * Set the authentication key pointing to the identity store. [required by SnakeYaml]
     *
     * @param authenticationKey the authentication key
     */
    public void setAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

}
