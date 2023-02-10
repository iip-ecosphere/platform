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

package de.iip_ecosphere.platform.services.environment;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Server process specification of servers to be started/stopped with an application.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlServer extends YamlProcess {

    private String id;
    private int port;
    private String host;
    private String cls;

    /**
     * Returns the id of the server, also to be used as network management key.
     * 
     * @return the id of the server
     */
    public String getId() {
        return id;
    }
    
    /**
     * Returns the network port of this server instance.
     * 
     * @return the network port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the host the server instance (may be superseded through a deployment plan).
     * 
     * @return the host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the class to be started as server. Must implement {@link Server}.
     * 
     * @return the class name
     */
    public String getCls() {
        return cls;
    }
    
    /**
     * Returns the id of the server, also to be used as network management key. [required by SnakeYaml]
     * 
     * @param id the id of the server
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Defines the network port of this server instance. [required by SnakeYaml]
     * 
     * @param port the network port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Defined the host the server instance (may be superseded through a deployment plan). [required by SnakeYaml]
     * 
     * @param host the host name
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * Defines the class to be started as server. Must implement {@link Server}.
     * 
     * @param cls the class name
     */
    public void setCls(String cls) {
        this.cls = cls;
    }

    /**
     * Turns this server into a temporary (partially default filled) service instance.
     * 
     * @return the service instance
     */
    public YamlService toService() {
        YamlService result = new YamlService();
        result.setDeployable(true);
        result.setDescription("");
        result.setVersion(new Version(0, 0, 0));
        result.setId(id);
        result.setName(id);
        result.setTopLevel(true);
        result.setProcess(this);
        return result;
    }
    
}
