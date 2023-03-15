/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.metrics.bitmotec;

import java.io.File;

import de.iip_ecosphere.platform.support.metrics.LinuxSystemMetricsUtils;
import de.iip_ecosphere.platform.support.metrics.SystemMetrics;

/**
 * System metrics implementation for Bitmotec.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BitmotecSystemMetrics implements SystemMetrics {

    public static final BitmotecSystemMetrics INSTANCE = new BitmotecSystemMetrics();
    
    private File cpuTempFile = null;
    private File boardTempFile = null;
    private int gpuCores = -1;

    /**
     * Prevents external creation.
     */
    protected BitmotecSystemMetrics() {
    }
    
    @Override
    public float getCaseTemperature() {
        boardTempFile = LinuxSystemMetricsUtils.getSysTempFile(boardTempFile, "acpi");
        return LinuxSystemMetricsUtils.getSysTemp(boardTempFile);
    }

    @Override
    public float getCpuTemperature() {
        cpuTempFile = LinuxSystemMetricsUtils.getSysTempFile(cpuTempFile, "x86");
        return LinuxSystemMetricsUtils.getSysTemp(cpuTempFile);
    }
    
    // future? Service: ws://172.16.1.1:4000/v2 - Method: getSystemSoftware - Params: []
    // actual: Container mit den passenden Berechtigungen (z.B. Docker Namespaces 
    // https://levelup.gitconnected.com/how-to-access-host-resources-from-a-docker-container-317e0d1f161e) oder 
    // weitreichenden Zugriffen (Volumes) auf "/sys" verwendet werden.
    
    @Override
    public int getNumGpuCores() {
        if (gpuCores < 0) {
            String tmp = LinuxSystemMetricsUtils.readStdoutFromProgram("", "nvidia-smi", "--list-gpus").trim();
            String[] lines = tmp.split("\r\n|\r|\n");
            gpuCores = lines.length;            
        }
        return gpuCores;
    }

    @Override
    public int getNumTpuCores() {
        return 0; // TODO
    }
    
    @Override
    public void close() {
    }

}
