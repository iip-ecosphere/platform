/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.processInfo;

import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Generic access to process level information. Requires an implementing plugin of type {@link ProcessInfoFactory} 
 * or an active {@link ProcessInfoFactoryProviderDescriptor}. 
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class ProcessInfoFactory {
    
    private static ProcessInfoFactory instance; 

    static {
        instance = PluginManager.getPluginInstance(ProcessInfoFactory.class, 
            ProcessInfoFactoryProviderDescriptor.class);
    }

    /**
     * Returns the Rest instance.
     * 
     * @return the instance
     */
    public static ProcessInfoFactory getInstance() {
        return instance;
    }
    
    /**
     * Manually sets the instance. Shall not be needed, but may be required in some tests.
     * 
     * @param rest the Rest instance
     */
    public static void setInstance(ProcessInfoFactory rest) {
        if (null != rest) {
            instance = rest;
        }
    }

    /**
     * Represents a process and its accessible information.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ProcessInfo {

        /**
         * Gets the Virtual Memory Size (VSZ). Includes all memory that the process can
         * access, including memory that is swapped out and memory that is from shared
         * libraries.
         *
         * @return the Virtual Memory Size
         */
        public long getVirtualSize();

    }

    /**
     * Creates a {@link ProcessInfo} object for the process with id {@code pid}.
     * 
     * @param pid the process id
     * @return the process info object
     */
    public abstract ProcessInfo create(long pid);
    
    /**
     * Creates a {@link ProcessInfo} object for the process {@code proc}.
     * 
     * @param proc the process to attach to
     * @return the process info object
     */
    public ProcessInfo create(Process proc) {
        return create(getProcessId(proc));        
    }
    
    /**
     * Returns the process id of the given process.
     * 
     * @param proc the process
     * @return the process id
     */
    public abstract long getProcessId(Process proc);
    
    /**
     * Returns the process id of this process.
     * 
     * @return the process id
     */
    public abstract long getProcessId();

}
