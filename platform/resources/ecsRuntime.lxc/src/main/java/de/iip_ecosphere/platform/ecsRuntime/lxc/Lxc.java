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
package de.iip_ecosphere.platform.ecsRuntime.lxc;

import de.iip_ecosphere.platform.ecsRuntime.EcsSetup.AbstractManagerSetup;

/**
 * Implements the LXC specific configuration.
 * 
 * @author Luca Schulz, SSE
 *
 */
public class Lxc extends AbstractManagerSetup {

    // TODO unify common parts with Lxc -> ecsRuntime???

    private String lxcHost = "localhost";
    private String lxcPort = "8443";

    /**
     * Returns the LXC port.
     * 
     * @return the LXC port as LXC port string, e.g., port
     */
    public String getLxcPort() {
        return lxcPort;
    }

    /**
     * Defines the LXC port.
     * 
     * @return the LXC port as LXC port string, e.g., 8443
     */
    public void setLxcPort(String lxcPort) {
        this.lxcPort = lxcPort;
    }

    private String lxcImageYamlFilename = "image-info.yml";
    private String downloadDirectory;

    /**
     * Returns the LXC host.
     * 
     * @return the LXChost as LXC host string, e.g., localhost
     */
    public String getLxcHost() {
        return lxcHost;
    }

    /**
     * Defines the LXC host.
     * 
     * @param lxcHost the LXC host as LXC host string, e.g., localhost
     */
    public void setLxcHost(String lxcHost) {
        this.lxcHost = lxcHost;
    }

    // Can possibly be removed, see during process
    /**
     * Returns the name of the Yaml file with information about the LXC Image.
     * 
     * @return Name of the Yaml file
     */
    public String getLxcImageYamlFilename() {
        return this.lxcImageYamlFilename;
    }

    /**
     * Defines the standard name of the Yaml file with a information about the LXC
     * Image.
     * 
     * @param filename the name of the Yaml file
     */
    public void setLxcImageYamlFilename(String filename) {
        this.lxcImageYamlFilename = filename;
    }

    // Can possibly be removed, see during process
    /**
     * Defines the download directory.
     * 
     * @param directory
     */
    public void setDownloadDirectory(String directory) {
        this.downloadDirectory = directory;
    }

    /**
     * Returns the download directory. If the configured download directory is
     * <b>null</b> or empty, it returns the system temporary directory.
     * 
     * @return directory, the system temporary directory if none is specified
     */
    public String getDownloadDirectory() {
        if (this.downloadDirectory == null || this.downloadDirectory.length() == 0) {
            return System.getProperty("java.io.tmpdir");
        }
        return this.downloadDirectory;
    }

}
