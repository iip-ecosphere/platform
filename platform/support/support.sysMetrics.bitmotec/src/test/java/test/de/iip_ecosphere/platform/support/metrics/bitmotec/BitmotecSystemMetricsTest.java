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

package test.de.iip_ecosphere.platform.support.metrics.bitmotec;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.metrics.SystemMetrics;
import de.iip_ecosphere.platform.support.metrics.SystemMetricsFactory;
import de.iip_ecosphere.platform.support.metrics.bitmotec.BitmotecSystemMetrics;

/**
 * Tests {@link BitmotecSystemMetrics}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BitmotecSystemMetricsTest {
    
    /**
     * Tests the uptake of {@link BitmotecSystemMetrics}.
     */
    @Test
    public void testSystemMetrics() {
        SystemMetrics m = SystemMetricsFactory.getSystemMetrics();
        Assert.assertTrue(m instanceof BitmotecSystemMetrics);
        
        // for an arbitrary testing system, we do not know the results, whether there are results, ... 
        m.getNumGpuCores();
        m.getCaseTemperature();
        m.getCpuTemperature();
        m.getNumGpuCores();
        m.getNumTpuCores();
        
        Assert.assertTrue(null != m.getOsArch() && m.getOsArch().length() > 0);
        Assert.assertTrue(null != m.getOsName() && m.getOsName().length() > 0);
    }
    
}
