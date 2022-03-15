/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
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
 * Access to operating system level information.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OsUtils {
    
    /**
     * Returns the operating system name.
     * 
     * @return the operating system name
     */
    public static final String getOsName() {
        // preliminary, may use org.apache.commons.lang.SystemUtils
        return System.getProperty("os.name", "");
    }
    
    /**
     * Returns the operating system architecture.
     * 
     * @return the operating system architecture.
     */
    public static final String getOsArch() {
        // preliminary, may use org.apache.commons.lang.SystemUtils
        return System.getProperty("os.arch", "");
    }
    
    /**
     * Returns the number of CPU cores.
     * 
     * @return the number of CPU cores
     */
    public static final int getNumCpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }
   
}
