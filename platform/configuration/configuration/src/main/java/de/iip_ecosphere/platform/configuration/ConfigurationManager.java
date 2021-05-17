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

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;

/**
 * Holds the platform configuration and provides operations on the configuration. The 
 * {@link ConfigurationLifecycleDescriptor} must be used and executed before.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationManager {
    
    private static EasyExecutor executor;
    private static boolean initialized = false;
    
    /**
     * Defines the executor instance. Called from {@link ConfigurationLifecycleDescriptor}.
     * @param instance
     */
    static void setExecutor(EasyExecutor instance) {
        executor = instance;
        if (null == instance) {
            initialized = false;
        }
    }
    
    /**
     * Returns the executor instance.
     * 
     * @return the executor instance
     */
    static EasyExecutor getExecutor() {
        return executor;
    }
    
    /**
     * Lazy initialization of the executor through loading the IVML model. Assumption: Locations are set before.
     */
    private static void init() {
        if (!initialized) {
            if (null != executor) {
                try {
                    executor.loadIvmlModel();
                } catch (ModelManagementException e) {
                    getLogger().error("Cannot load EASy-Producer models: " + e.getMessage());
                }
            }
            initialized = true;
        }
    }
    
    /**
     * Returns the IVML configuration.
     * 
     * @return the configuration
     */
    public static Configuration getIvmlConfiguration() {
        init();
        return executor != null ? executor.getConfiguration() : null;
    }
    
    /**
     * Validates the model and propagates values within the model.
     * 
     * @return the reasoning result (preliminary)
     */
    public static ReasoningResult validateAndPropagate() {
        init();
        return executor != null ? executor.propagateOnIvmlModel() : null;
    }
    
    /**
     * Performs a platform instantiation.
     * 
     * @throws ExecutionException if the instantiation fails for some reason
     */
    public static void instantiate() throws ExecutionException {
        init();
        if (executor != null) {
            try {
                executor.executeVil();
            } catch (ModelManagementException | VilException e) {
                throw new ExecutionException(e);
            }
        }
    }

    /**
     * Returns the logger for this class.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(ConfigurationLifecycleDescriptor.class);
    }

}
