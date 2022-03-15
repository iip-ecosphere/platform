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

package test.de.iip_ecosphere.platform.support;

import de.iip_ecosphere.platform.support.metrics.SystemMetrics;
import de.iip_ecosphere.platform.support.metrics.SystemMetricsFactory;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link SystemMetricsFactory}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SystemMetricsTest {

    /**
     * Tests the access to and response of {@link SystemMetricsFactory#getSystemMetrics()}.
     */
    @Test
    public void testSystemMetrics() {
        SystemMetrics m = SystemMetricsFactory.getSystemMetrics();
        Assert.assertNotNull(m);
        testImplemented(m);

        // just write out those that we do not know
        System.out.println("temp case: " + m.getCaseTemperature());
        System.out.println("temp CPU: " + m.getCpuTemperature());
        System.out.println("temp CPU: " + m.getNumGpuCores());
    }
    
    /**
     * Tests the metrics implemented in this component.
     * 
     * @param metrics the system metrics
     */
    public static void testImplemented(SystemMetrics metrics) {
        String osName = metrics.getOsName();
        Assert.assertNotNull(osName);
        Assert.assertTrue(osName.length() > 0);
        System.out.println("OS: " + osName);

        String osArch = metrics.getOsArch();
        Assert.assertNotNull(osArch);
        Assert.assertTrue(osArch.length() > 0);
        System.out.println("Arch: " + osArch);

        int cpuNum = metrics.getNumCpuCores();
        Assert.assertTrue(cpuNum >= 0); // well, seems to be 0 on VMs :/
        System.out.println("#CPU: " + cpuNum);        
    }
    
}
