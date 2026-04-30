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

import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.metrics.SystemMetrics;
import jcuda.CudaException;
import jcuda.driver.CUdevice;
import jcuda.driver.CUdevice_attribute;
import jcuda.driver.JCudaDriver;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Sensors;

/**
 * Default system metrics with a basic non-JDK provided metrics implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultSystemMetrics implements SystemMetrics {

    public static final SystemMetrics INSTANCE = new DefaultSystemMetrics();
    
    private static oshi.SystemInfo sysInfo;
    private static boolean cudaWarn = false;

    /**
     * Prevents external creation.
     */
    protected DefaultSystemMetrics() {
    }
    
    @Override
    public float getCaseTemperature() {
        return INVALID_CELSIUS_TEMPERATURE; // we don't know
    }

    @Override
    public float getCpuTemperature() {
        if (null == sysInfo) {
            sysInfo = new oshi.SystemInfo();
        }
        HardwareAbstractionLayer hal = sysInfo.getHardware();
        Sensors sensors = hal.getSensors();
        return (float) sensors.getCpuTemperature();
    }
    
    @Override
    public int getNumGpuCores() {
        int result = 0;
        try {
            JCudaDriver.setExceptionsEnabled(true);
            JCudaDriver.cuInit(0);
    
            CUdevice device = new CUdevice();
            JCudaDriver.cuDeviceGet(device, 0);
    
            int[] smCount = {0};
            JCudaDriver.cuDeviceGetAttribute(
                smCount,
                CUdevice_attribute.CU_DEVICE_ATTRIBUTE_MULTIPROCESSOR_COUNT,
                device
            );
            result = smCount[0];
        } catch (UnsatisfiedLinkError e) {
            if (!cudaWarn) {
                LoggerFactory.getLogger(DefaultSystemMetrics.class).warn("{}, defaulting to 0", e.getMessage());
                cudaWarn = true;
            }
        } catch (CudaException e) {
            result = 0;
        }
        return result;
    }
    
    @Override
    public int getNumCpuCores() {
        return OsUtils.getNumCpuCores();
    }

}
