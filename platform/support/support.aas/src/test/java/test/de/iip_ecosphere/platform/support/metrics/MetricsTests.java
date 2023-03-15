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

package test.de.iip_ecosphere.platform.support.metrics;

import org.junit.Test;

import de.iip_ecosphere.platform.support.metrics.DefaultSystemMetrics;
import de.iip_ecosphere.platform.support.metrics.DefaultSystemMetricsDescriptor;
import de.iip_ecosphere.platform.support.metrics.LinuxSystemMetricsUtils;
import de.iip_ecosphere.platform.support.metrics.SystemMetrics;

import java.io.File;

import org.apache.commons.lang.SystemUtils;
import org.junit.Assert;

/**
 * Basic metrics tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MetricsTests {
    
    /**
     * Tests {@link DefaultSystemMetricsDescriptor} and {@link DefaultSystemMetrics}.
     */
    @Test
    public void testDefaultSystemMetrics() {
        DefaultSystemMetricsDescriptor desc = new DefaultSystemMetricsDescriptor();
        Assert.assertFalse(desc.isEnabled());
        Assert.assertTrue(desc.isFallback());
        SystemMetrics inst = desc.createInstance();
        Assert.assertNotNull(inst);
        Assert.assertEquals(SystemMetrics.INVALID_CELSIUS_TEMPERATURE, inst.getCaseTemperature(), 0.01);
        Assert.assertEquals(SystemMetrics.INVALID_CELSIUS_TEMPERATURE, inst.getCpuTemperature(), 0.01);
        Assert.assertTrue(null != inst.getOsArch() && inst.getOsArch().length() > 0);
        Assert.assertTrue(null != inst.getOsName() && inst.getOsName().length() > 0);
        Assert.assertTrue(inst.getNumCpuCores() > 0); // shall usually be the case
        inst.getNumGpuCores(); // we do not know for an arbitrary system
        inst.getNumTpuCores(); // we do not know for an arbitrary system
    }
    
    /**
     * Tests {@link LinuxSystemMetricsUtils}.
     */
    @Test
    public void testLinuxSystemMetricsUtils() {
        String old = LinuxSystemMetricsUtils.setThermalFolder("./src/test/resources/metrics/sys/thermal");
        Assert.assertNotNull(old);
        
        File tempFile = LinuxSystemMetricsUtils.getSysTempFile(null, "x86");
        Assert.assertNotNull(tempFile);
        File tempFile2 = LinuxSystemMetricsUtils.getSysTempFile(tempFile, "x86");
        Assert.assertEquals(tempFile2, tempFile); // pass through
        Assert.assertTrue(LinuxSystemMetricsUtils.getSysTemp(tempFile) > 10); // 27...
        Assert.assertTrue(LinuxSystemMetricsUtils.getSysTemp(tempFile) < 100); // 27...
        
        String[] args;
        if (SystemUtils.IS_OS_WINDOWS) {
            args = new String[] {"cmd.exe", "/c", "dir"}; 
        } else {
            args = new String[] {"ls"}; 
        }
        String res = LinuxSystemMetricsUtils.readStdoutFromProgram("***", args);
        Assert.assertNotNull(res);
        Assert.assertTrue(!res.equals("***")); // we expect some directory listing, contents irrelevant
        
        Assert.assertEquals(-1, LinuxSystemMetricsUtils.readIntStdoutFromProgram(-1, args)); // shall always "fail"
        Assert.assertEquals(1234, LinuxSystemMetricsUtils.readIntStdoutFromProgram(-1, s -> "1234", args)); // ignores
        
        LinuxSystemMetricsUtils.setThermalFolder(old);
    }

}
