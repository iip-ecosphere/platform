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

package de.iip_ecosphere.platform.support.metrics.bitmotec;

import java.io.File;

import de.iip_ecosphere.platform.support.metrics.SystemMetrics;
import de.iip_ecosphere.platform.support.metrics.SystemMetricsDescriptor;

/**
 * The default JSL system metrics descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BitmotecSystemMetricsDescriptor implements SystemMetricsDescriptor {

    @Override
    public SystemMetrics createInstance() {
        return BitmotecSystemMetrics.INSTANCE;
    }

    @Override
    public boolean isEnabled() {
        File f1 = new File("/etc/os-release"); // as discussed with Bitmotec
        // /etc/core/settings.env
        return f1.exists(); // look into file
    }

    @Override 
    public boolean isFallback() {
        return false; // act as fallback if there is no specific enabled one
    }

}