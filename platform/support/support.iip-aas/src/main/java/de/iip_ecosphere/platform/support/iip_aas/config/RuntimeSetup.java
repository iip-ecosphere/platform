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

package de.iip_ecosphere.platform.support.iip_aas.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import de.iip_ecosphere.platform.support.PidFile;
import de.iip_ecosphere.platform.support.setup.AbstractSetup;

/**
 * Runtime information produced, stored, consumed by the platform.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RuntimeSetup extends AbstractSetup {
    
    private static RuntimeSetup instance;
    private String aasRegistry;
    private String smRegistry;
    private String aasServer;
    private String smServer;

    /**
     * Returns the AAS registry URI.
     * 
     * @return the registry URI (may be <b>null</b> for unknown)
     */
    public String getAasRegistry() {
        return aasRegistry;
    }

    /**
     * Defines the AAS registry URI. [snakeyaml]
     * 
     * @param aasRegistry the AAS registry URI to set
     */
    public void setAasRegistry(String aasRegistry) {
        this.aasRegistry = aasRegistry;
    }

    /**
     * Returns the AAS registry URI.
     * 
     * @return the registry URI (may be <b>null</b> for unknown)
     */
    public String getSubmodelRegistry() {
        return smRegistry;
    }

    /**
     * Defines the AAS registry URI. [snakeyaml]
     * 
     * @param smRegistry the AAS registry URI to set
     */
    public void setSubmodelRegistry(String smRegistry) {
        this.smRegistry = smRegistry;
    }

    /**
     * Returns the AAS server URI.
     * 
     * @return the AAS server URI (may be <b>null</b> for unknown)
     */
    public String getAasServer() {
        return aasServer;
    }

    /**
     * Defines the AAS server URI. [snakeyaml]
     * 
     * @param aasServer the aasServer URI to set
     */
    public void setAasServer(String aasServer) {
        this.aasServer = aasServer;
    }
    
    /**
     * Returns the AAS server URI.
     * 
     * @return the AAS server URI (may be <b>null</b> for unknown)
     */
    public String getSubmodelServer() {
        return smServer;
    }

    /**
     * Defines the AAS server URI. [snakeyaml]
     * 
     * @param smServer the server URI to set
     */
    public void setSubmodelServer(String smServer) {
        this.smServer = smServer;
    }    
    
    /**
     * Returns the default location for the runtime setup file.
     * 
     * @return the location
     */
    public static File getFile() {
        return new File(PidFile.getPidDirectory(), "oktoflow.yaml");
    }

    /**
     * Stores this runtime setup to {@link #getFile()}.
     */
    public void store() {
        Constructor constructor = new Constructor(RuntimeSetup.class);
        TypeDescription configDescription = new TypeDescription(RuntimeSetup.class);
        constructor.addTypeDescription(configDescription);
        Yaml yaml = new Yaml(constructor);
        try (FileWriter writer = new FileWriter(getFile())) {
            yaml.dump(this, writer);      
        } catch (IOException e) {
            LoggerFactory.getLogger(RuntimeSetup.class).warn("Cannot write platform runtime setup {}: {} "
                + "Ephemeral AAS ports may not work.", getFile(), e.getMessage());
        }
    }
    
    /**
     * Clears the instance. [testing]
     */
    public static void clear() {
        instance = null;
    }

    /**
     * Loads the runtime setup from {@link #getFile()}.
     * 
     * @return the runtime setup, may be a default instance
     */
    public static RuntimeSetup load() {
        return load(() -> new RuntimeSetup(), true);
    }
    
    /**
     * Loads the runtime setup from {@link #getFile()}.
     * 
     * @param onFailure supplies the instance to be returned in case of a failure
     * @param logOnFailure if failures shall be logged
     * @return the runtime setup, may be a default instance
     */
    public static synchronized RuntimeSetup load(Supplier<RuntimeSetup> onFailure, boolean logOnFailure) {
        if (null == instance) {
            try (FileInputStream in = new FileInputStream(getFile())) {
                instance = AbstractSetup.readFromYaml(RuntimeSetup.class, in);
                LoggerFactory.getLogger(RuntimeSetup.class).info("Considering runtime setup from {}", getFile());
            } catch (IOException e) {
                if (logOnFailure) {
                    LoggerFactory.getLogger(RuntimeSetup.class).warn("Cannot read platform runtime setup {}: {} "
                        + "Ephemeral AAS ports may not work.", getFile(), e.getMessage());
                }
                instance = onFailure.get();
            }
        }
        return instance;
    }

}
