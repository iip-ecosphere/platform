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

package test.de.iip_ecosphere.platform.support.dfltSysMetrics;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.dfltSysMetrics.DefaultSystemMetrics;
import de.iip_ecosphere.platform.support.metrics.SystemMetrics;
import de.iip_ecosphere.platform.support.metrics.SystemMetricsFactory;
import test.de.iip_ecosphere.platform.support.SystemMetricsTest;

/**
 * Tests {@link DefaultSystemMetrics}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultSystemMetricsTest {
    
    /**
     * Tests the uptake of {@link DefaultSystemMetrics}.
     */
    @Test
    public void testSystemMetrics() {
        SystemMetrics m = SystemMetricsFactory.getSystemMetrics();
        Assert.assertTrue(m instanceof DefaultSystemMetrics);
        
        SystemMetricsTest.testImplemented(m);
        
        int gpuNum = m.getNumGpuCores();
        Assert.assertTrue(gpuNum >= 0); // does not make much sense, but is optional
        System.out.println("#GPU: " + gpuNum); 

        int tpuNum = m.getNumTpuCores();
        Assert.assertTrue(tpuNum >= 0); // does not make much sense, but is optional
        System.out.println("#TPU: " + tpuNum); 

        float cpuTemp = m.getCpuTemperature();
        // shall be the case, but only if executed in admin mode
        Assert.assertTrue(cpuTemp >= SystemMetrics.INVALID_CELSIUS_TEMPERATURE); 
        System.out.println("temp CPU: " + cpuTemp); 

        System.out.println("temp case: " + m.getCaseTemperature()); // not implemented here
    }
    
}
