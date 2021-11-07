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

import de.iip_ecosphere.platform.deviceMgt.DeviceMgtSetup;
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

    private static final StorageFactory INSTANCE = new StorageFactory();
    private Storage runtimeStorage;
    private Storage configStorage;
    private DeviceMgtSetup setup;
    private StorageFactoryDescriptor desc;

    // public constructor for testing
    
    /**
     * Returns the instance of this factory.
     * 
     * @return the instance
     */
    public static StorageFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a runtime storage with the help of the service provider.
     * If no service provider is found, it will fall back to the
     * S3StorageFactoryDescriptor as a default.
     *
     * @return the runtime storage
     */
    public Storage createRuntimeStorage() {
        loadSetup();
        initDesc();

        if (runtimeStorage == null && setup != null && desc != null) {
            runtimeStorage = desc.createRuntimeStorage(setup);
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
        loadSetup();
        initDesc();

        if (configStorage == null && setup != null && desc != null) {
            configStorage = desc.createConfigStorage(setup);
        }
        return configStorage;
    }

    /**
     * Loads the setup.
     */
    private void loadSetup() {
        if (setup == null) {
            try {
                setup = DeviceMgtSetup.readFromYaml(DeviceMgtSetup.class);
            } catch (IOException e) {
                LOGGER.error("Cannot load Configuration: ", e);
            }
        }
    }

    /**
     * Sets the configuration, only used in testing. [public for testing]
     *
     * @param configuration the configuration
     */
    public void setSetup(DeviceMgtSetup configuration) {
        this.setup = configuration;
    }

    /**
     * Returns the setup instance.
     * 
     * @return the setup instance, <b>null</b> if none was loaded/none is available
     */
    public DeviceMgtSetup getSetup() {
        loadSetup();
        return setup;
    }

}
