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

package de.iip_ecosphere.platform.support.metrics.plcnext;

import de.iip_ecosphere.platform.support.metrics.SystemMetrics;

/**
 * System metrics implementation for Phoenix Contact/PLCnext.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlcNextSystemMetrics implements SystemMetrics {

    public static final SystemMetrics INSTANCE = new PlcNextSystemMetrics();

    /**
     * Prevents external creation.
     */
    protected PlcNextSystemMetrics() {
    }
    
    @Override
    public float getCaseTemperature() {
        return 0; // TODO
    }

    @Override
    public float getCpuTemperature() {
        return 0; // TODO
    }
    
    @Override
    public int getNumGpuCores() {
        return 0; // TODO
    }
    
    @Override
    public int getNumCpuCores() {
        return 0;  // TODO
    }

}