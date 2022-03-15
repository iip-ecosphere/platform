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

package de.iip_ecosphere.platform.support.dfltSysMetrics;

import com.profesorfalken.jsensors.JSensors;
import com.profesorfalken.jsensors.model.components.Components;
import com.profesorfalken.jsensors.model.components.Cpu;
import com.profesorfalken.jsensors.model.sensors.Temperature;

import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.metrics.SystemMetrics;

/**
 * Default system metrics with a basic non-JDK provided metrics implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultSystemMetrics implements SystemMetrics {

    public static final SystemMetrics INSTANCE = new DefaultSystemMetrics();
    
    private Components components = JSensors.get.components();

    /**
     * Prevents external creation.
     */
    protected DefaultSystemMetrics() {
    }
    
    @Override
    public float getCaseTemperature() {
        return 0; // we don't know
    }

    @Override
    public float getCpuTemperature() {
        float result = 0;
        float count = 0;
        // just the average CPUs temperature for now
        if (components.cpus != null) {
            for (Cpu cpu : components.cpus) {
                if (cpu.sensors != null) {
                    for (Temperature tmp : cpu.sensors.temperatures) {
                        result += tmp.value;
                    }
                }
            }
        }
        return count == 0 ? 0 : result / count;
    }
    
    @Override
    public int getNumGpuCores() {
        return null != components.gpus ? components.gpus.size() : 0;
    }
    
    @Override
    public int getNumCpuCores() {
        // for consistency, neiter seems to work on VMs
        return null != components.cpus ? components.cpus.size() : OsUtils.getNumCpuCores();
    }

}
