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

package de.iip_ecosphere.platform.configuration;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.deviceMgt.storage.PackageStorageSetup;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * The setup for the configuration component. As there are also EASy-Producer configuration classes with different 
 * meaning, we did not call this class "Configuration" or "ConfigurationConfiguration".
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationSetup extends AbstractSetup {

    private static ConfigurationSetup instance;
    private EasySetup easyProducer = new EasySetup();
    private PackageStorageSetup serviceArtifactStorage;
    private PackageStorageSetup containerImageStorage;

    /**
     * Creates an instance.
     */
    public ConfigurationSetup() {
        easyProducer.reset();
    }
    
    /**
     * Returns the EASy-Producer setup.
     * 
     * @return the setup
     */
    public EasySetup getEasyProducer() {
        return easyProducer;
    }

    /**
     * Returns the EASy-Producer setup. [snakyaml]
     * 
     * @param easyProducer the storage setup
     */
    public void setEasyProducer(EasySetup easyProducer) {
        this.easyProducer = easyProducer;
    }
    
    /**
     * Returns the EASy-Producer setup.
     * 
     * @return the setup
     */
    public PackageStorageSetup getServiceArtifactStorage() {
        return serviceArtifactStorage;
    }

    /**
     * Returns the EASy-Producer setup. [snakyaml]
     * 
     * @param serviceArtifactStorage the storage setup
     */
    public void setServiceArtifactStorage(PackageStorageSetup serviceArtifactStorage) {
        this.serviceArtifactStorage = serviceArtifactStorage;
    }

    /**
     * Returns the container image storage.
     * 
     * @return the setup
     */
    public PackageStorageSetup getContainerImageStorage() {
        return containerImageStorage;
    }

    /**
     * Returns the setup for the global container image storage. [snakyaml]
     * 
     * @param containerImageStorage the setup
     */
    public void setContainerImageStorage(PackageStorageSetup containerImageStorage) {
        this.containerImageStorage = containerImageStorage;
    }

    /**
     * Returns the configuration instance, may read it the first time from a default "configuration.yml" file in the 
     * root folder of the containing jar. 
     *
     * @return the configuration instance
     */
    public static ConfigurationSetup getSetup() {
        if (null == instance) {
            try {
                instance = readFromYaml(ConfigurationSetup.class);
            } catch (IOException e) {
                LoggerFactory.getLogger(ConfigurationSetup.class).warn("Fallback to default config: " + e.getMessage());
                instance = new ConfigurationSetup();
            }
        }
        return instance;
    }

}
