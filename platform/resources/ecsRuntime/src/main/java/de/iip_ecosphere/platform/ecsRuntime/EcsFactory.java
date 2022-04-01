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

package de.iip_ecosphere.platform.ecsRuntime;

import java.io.IOException;
import java.util.Optional;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

/**
 * Provides access to the ECS instances.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EcsFactory.class.getName());
    private static EcsFactoryDescriptor desc;
    private static ContainerManager manager = null;
    private static EcsSetup conf;

    /**
     * Initializes this factory.
     */
    private static void init() {
        if (null == desc) {
            ServiceLoader<EcsFactoryDescriptor> loader = ServiceLoader.load(EcsFactoryDescriptor.class);
            Optional<EcsFactoryDescriptor> first = ServiceLoaderUtils.findFirst(loader);
            if (first.isPresent()) {
                desc = first.get();
            } else {
                LOGGER.info("No container manager available. No container operations offered in AAS.");
            }
        }
        if (null == conf) {
            if (null != desc) {
                conf = desc.getConfiguration();
            } else {
                try {
                    conf = AbstractSetup.readFromYaml(EcsSetup.class);
                } catch (IOException e) {
                    conf = new EcsSetup();
                    LOGGER.error("No configuration, falling back to default " + e.getMessage());
                }
            }
        }
    }

    /**
     * Returns the service manager.
     * 
     * @return the service manager (may be <b>null</b> if there is intentionally no container manager)
     */
    public static ContainerManager getContainerManager() {
        if (null == manager) {
            init();
            if (null != desc) {
                manager = desc.createContainerManagerInstance();
                if (null != manager) {
                    LOGGER.info("Container manager implementation registered: " + manager.getClass().getName());
                }
            }
        }
        return manager;
    }

    /**
     * Returns the actual setup instance for the implementing container manager.
     * 
     * @return the setup instance
     */
    public static EcsSetup getSetup() {
        if (null == conf) {
            init();
        }
        return conf;
    }

}
