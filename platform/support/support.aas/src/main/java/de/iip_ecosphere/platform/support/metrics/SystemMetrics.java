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

package de.iip_ecosphere.platform.support.metrics;

import de.iip_ecosphere.platform.support.OsUtils;

/**
 * Provides uniform access to static and dynamic system metrics.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface SystemMetrics {

    public static final float INVALID_CELSIUS_TEMPERATURE = -274;
    
    /**
     * Returns whether a temperature can be considered valid.
     * 
     * @param value the value to be tested
     * @return {@code true} for valid, {@code false} for invalid
     */
    public static boolean isCelsiusTemperatureValid(float value) {
        return value > INVALID_CELSIUS_TEMPERATURE;
    }
    
    /**
     * Returns the operating system name.
     * 
     * @return the operating system name (valid = non-empty)
     */
    public default String getOsName() {
        return OsUtils.getOsName();
    }
    
    /**
     * Returns the operating system architecture.
     * 
     * @return the operating system architecture (valid = non-empty)
     */
    public default String getOsArch() {
        return OsUtils.getOsArch();
    }
    
    /**
     * Returns the number of CPU cores.
     * 
     * @return the number of CPU cores (valid = positive)
     */
    public default int getNumCpuCores() {
        return OsUtils.getNumCpuCores();
    }
    
    /**
     * Returns the number of CPU cores.
     * 
     * @return the number of CPU cores (valid = non-negative)
     */
    public int getNumGpuCores();

    /**
     * Returns the number of CPU cores.
     * 
     * @return the number of CPU cores (valid = non-negative)
     */
    public default int getNumTpuCores() {
        return 0;
    }

    /**
     * Returns the case temperature.
     * 
     * @return case temperature in degrees centigrade (invalid = {@link #INVALID_CELSIUS_TEMPERATURE})
     */
    public float getCaseTemperature();

    /**
     * Returns the CPU temperature.
     * 
     * @return CPU temperature in degrees centigrade (invalid = {@link #INVALID_CELSIUS_TEMPERATURE})
     */
    public float getCpuTemperature();
    
    /**
     * Closes this metrics plugin, e.g., if resources have to be freed.
     */
    public default void close() {
    }

}
