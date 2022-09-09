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

import java.io.File;

import de.iip_ecosphere.platform.support.metrics.SystemMetrics;
import de.iip_ecosphere.platform.support.metrics.SystemMetricsDescriptor;

/**
 * The default JSL system metrics descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlcNextSystemMetricsDescriptor implements SystemMetricsDescriptor {

    @Override
    public SystemMetrics createInstance() {
        return PlcNextSystemMetrics.INSTANCE;
    }

    @Override
    public boolean isEnabled() {
        // if native on plcnext, but may not be in container; still PLCNEXT_SOCK may not be there
        File f1 = new File("/opt/plcnext"); // as discussed with Phoenix Contact
        // fallback - if not there, no metrics; if there, use it anyway
        File f2 = new File(PlcNextSystemMetrics.PLCNEXT_SOCK); 
        return f1.exists() || f2.exists(); 
    }

    @Override 
    public boolean isFallback() {
        return false; // act as fallback if there is no specific enabled one
    }

}
