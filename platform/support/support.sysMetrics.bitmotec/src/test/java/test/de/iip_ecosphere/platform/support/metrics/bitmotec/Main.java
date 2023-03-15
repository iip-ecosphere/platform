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

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.metrics.SystemMetrics;
import de.iip_ecosphere.platform.support.metrics.bitmotec.BitmotecSystemMetricsDescriptor;

/**
 * Simple main program for command line testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Main {
    
    /**
     * Simple main program for command line testing.
     * 
     * @param args
     */
    public static void main(String[] args) {
        BitmotecSystemMetricsDescriptor desc = new BitmotecSystemMetricsDescriptor();
        System.out.println("is enabled: " + desc.isEnabled() + " is fallback " + desc.isFallback());
        SystemMetrics metrics = desc.createInstance();
        for (int i = 0; i < 10; i++) {
            System.out.println("cpu cores: " + metrics.getNumCpuCores() + " gpu: " + metrics.getNumGpuCores() 
                + " tpu: " + metrics.getNumTpuCores() + " case temp: "
                + metrics.getCaseTemperature() + " cpu tmp: " + metrics.getCpuTemperature());
            TimeUtils.sleep(1000);
        }
        metrics.close();
    }

}
