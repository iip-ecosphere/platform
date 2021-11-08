/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support;

/**
 * A descriptor interface to hook into certain program/application lifecycle phases. In particular, application 
 * frameworks such as spring require the execution of specific code in certain lifecycle phases. However, this is
 * difficult to handle if such application frameworks are used as options/alternatives. Such code shall be linked
 * to {@link LifecycleDescriptor lifecycle descriptors} and executed via {@link LifecycleHandler}. 
 * {@link LifecycleDescriptor} shall be declared via Java Service Loading.
 * 
 * Optionally, a lifecycle descriptor may be annotated with {@link LifecycleExclude} to prevent other lifecycle 
 * handlers from execution in certain situations.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface LifecycleDescriptor {

    /**
     * Directly interact with the command line, ability to start/stop the program before internal services come up.
     */
    public static final int CMD_LINE_PRIORITY = 0;
    
    /**
     * Initialize the program, e.g., start internal services (after {@link #CMD_LINE_PRIORITY}).
     */
    public static final int INIT_PRIORITY = 1;

    /**
     * Initialize the AAS/submodels, in particular after all descriptors with {@link #INIT_PRIORITY}.
     */
    public static final int AAS_PRIORITY = 100;
    
    // Java Service Loading, do not change class name/package!
    
    /**
     * Called during startup to process initialization work and to consider the given command line arguments.
     * 
     * @param args the command line arguments
     */
    public void startup(String[] args);
    
    /**
     * Called during shutdown. 
     */
    public void shutdown();
    
    /**
     * Optional shutdown hook to be executed during JVM shutdown. Please consider that shutdown hooks are not 
     * guaranteed to be executed.
     *  
     * @return the shutdown hook, may be <b>null</b> for none
     */
    public Thread getShutdownHook();

    /**
     * Returns a numeric priority to establish an order of lifecycle descriptors.
     * 
     * @return a numeric priority
     */
    public int priority();
    
}
