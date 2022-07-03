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

import de.iip_ecosphere.platform.support.metrics.SystemMetrics;
import de.iip_ecosphere.platform.support.metrics.SystemMetricsDescriptor;

/**
 * The default JSL system metrics descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultSystemMetricsDescriptor implements SystemMetricsDescriptor {

    @Override
    public SystemMetrics createInstance() {
        return new DefaultSystemMetrics();
    }

    @Override
    public boolean isEnabled() {
        return false; // do not override system specific one
    }

    @Override 
    public boolean isFallback() {
        return true; // act as fallback if there is no specific enabled one
    }

}
