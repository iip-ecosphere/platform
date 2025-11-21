/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.monitoring.prometheus;

import java.util.List;

import de.iip_ecosphere.platform.monitoring.MonitoringDescriptor;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;

/**
 * The JSL plugin descriptor for prometheus. Currently there is no need to hook in as monitoring is an additional 
 * server, but this may change.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PrometheusMonitoringPluginDescriptor extends SingletonPluginDescriptor<MonitoringDescriptor> {

    /**
     * Creates the descriptor via JSL.
     */
    public PrometheusMonitoringPluginDescriptor() {
        super("monitoring", List.of("monitoring-prometheus"), MonitoringDescriptor.class, 
            p -> new MonitoringDescriptor() { });
    }

}
