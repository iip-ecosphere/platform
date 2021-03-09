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
 * @author Holger Eichelberger, SSE
 */
public interface LifecycleDescriptor {
    
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

}
