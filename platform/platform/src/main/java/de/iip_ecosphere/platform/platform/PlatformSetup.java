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

package de.iip_ecosphere.platform.platform;

import java.io.File;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * Platform setup from YAML.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformSetup extends AbstractSetup {

    /**
     * Common persistence types.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum ConfiguredPersistenceType {
        INMEMORY,
        MONGO // let's see for other types, may be we need some exclusions on the configuration level
    }
    
    private static PlatformSetup instance;
    private PersistentAasSetup aas = new PersistentAasSetup();
    private TransportSetup transport;
    private File artifactsFolder = new File("artifacts");
    private String artifactsUriPrefix = "";
    private int aasHeartbeatTimeout = -1; //4000; // disabled for now
    private int aasStatusTimeout = 2 * 60 * 1000;
    
    /**
     * Returns the AAS setup.
     * 
     * @return the AAS setup
     */
    public PersistentAasSetup getAas() {
        return aas;
    }

    /**
     * Returns the transport setup.
     * 
     * @return the transport setup
     */
    public TransportSetup getTransport() {
        return transport;
    }
    
    /**
     * Returns the folder containing installable artifacts.
     * 
     * @return the folder
     */
    public File getArtifactsFolder() {
        return artifactsFolder;
    }
    
    /**
     * Returns the artifacts URI prefix.
     * 
     * @return the prefix with protocol, may be empty for none
     */
    public String getArtifactsUriPrefix() {
        return artifactsUriPrefix;
    }

    /**
     * Defines the transport setup. [snakeyaml]
     * 
     * @param transport the transport setup
     */
    public void setTransport(TransportSetup transport) {
        this.transport = transport;
    }
    
    /**
     * Defines the AAS setup. [snakeyaml]
     * 
     * @param aas the AAS setup
     */
    public void setAas(PersistentAasSetup aas) {
        this.aas = aas;
    }
    
    /**
     * Changes the folder containing installable artifacts. [snakeyaml]
     * 
     * @param artifactsFolder the folder
     */
    public void setArtifactsFolder(File artifactsFolder) {
        this.artifactsFolder = artifactsFolder;
    }
    
    /**
     * Changes the artifacts URI prefix. [snakeyaml]
     * 
     * @param artifactsUriPrefix the prefix with protocol, may be empty for none
     */
    public void setArtifactsUriPrefix(String artifactsUriPrefix) {
        this.artifactsUriPrefix = artifactsUriPrefix;
    }
    
    /**
     * Reads once an instance from a default "platform.yml" file in the root folder of the jar. 
     *
     * @return the configuration instance
     * @see #readFromYaml(Class)
     */
    public static PlatformSetup getInstance() {
        if (null == instance) {
            try {
                instance = readFromYaml(PlatformSetup.class);
            } catch (IOException e) {
                LoggerFactory.getLogger(PlatformSetup.class).error(
                    "Cannot start AAS server: " + e.getMessage());
                instance = new PlatformSetup();
            }
        }
        return instance;
    }
    
    /**
     * Returns the AAS heartbeat/cleanup timeout.
     * 
     * @return the timeout in ms, shall be significantly larger than the ECS-Runtime/service manager 
     * monitoring periods, disable heartbeat monitoring if not positive
     */
    public int getAasHeartbeatTimeout() {
        return aasHeartbeatTimeout;
    }

    /**
     * Changes the AAS heartbeat/cleanup timeout. [snakeyaml]
     * 
     * @param aasHeartbeatTimeout the timeout in ms, shall be significantly larger than the ECS-Runtime/service manager 
     * monitoring periods, disable heartbeat monitoring if not positive
     */
    public void setAasHeartbeatTimeout(int aasHeartbeatTimeout) {
        this.aasHeartbeatTimeout = aasHeartbeatTimeout;
    }

    /**
     * Returns the AAS status list cleanup timeout.
     * 
     * @return the timeout in ms
     */
    public int getAasStatusTimeout() {
        return aasStatusTimeout;
    }

    /**
     * Changes the AAS status list cleanup timeout. [snakeyaml]
     * 
     * @param aasStatusTimeout the timeout in ms
     */
    public void setAasStatusTimeout(int aasStatusTimeout) {
        this.aasStatusTimeout = aasStatusTimeout;
    }

}
