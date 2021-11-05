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

import de.iip_ecosphere.platform.deviceMgt.Configuration;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * A StorageFactory is capable of creating different kinds of storages.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class StorageFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageFactory.class.getName());

    private Storage runtimeStorage;
    private Storage configStorage;
    private Configuration configuration;
    private StorageFactoryDescriptor desc;

    /**
     * Creates a runtime storage with the help of the service provider.
     * If no service provider is found, it will fall back to the
     * S3StorageFactoryDescriptor as a default.
     *
     * @return the runtime storage
     */
    public Storage createRuntimeStorage() {
        loadConfiguration();
        initDesc();

        if (runtimeStorage == null && configuration != null && desc != null) {
            runtimeStorage = desc.createRuntimeStorage(configuration);
        }
        return runtimeStorage;
    }

    /**
     * Initializes the descriptors.
     */
    private void initDesc() {
        if (desc == null) {
            Optional<StorageFactoryDescriptor> storageFactoryDescriptors =
                    ServiceLoaderUtils.findFirst(StorageFactoryDescriptor.class);
            if (storageFactoryDescriptors.isPresent()) {
                desc = storageFactoryDescriptors.get();
            } else {
                LOGGER.info("No StorageFactoryDescriptor implementation available");
            }
        }
    }

    /**
     * Creates a config storage with the help of the service provider.
     * If no service provider is found, it will fall back to the
     * S3StorageFactoryDescriptor as a default.
     *
     * @return the runtime storage
     */
    public Storage createConfigStorage() {
        loadConfiguration();
        initDesc();

        if (configStorage == null && configuration != null && desc != null) {
            configStorage = desc.createConfigStorage(configuration);
        }
        return configStorage;
    }

    /**
     * Loads the configuration.
     */
    private void loadConfiguration() {
        if (configuration == null) {
            try {
                configuration = Configuration.readFromYaml(Configuration.class);
            } catch (IOException e) {
                LOGGER.error("Cannot load Configuration: ", e);
            }
        }
    }

    /**
     * Sets the configuration, only used in testing.
     *
     * @param configuration the configuration
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

}
