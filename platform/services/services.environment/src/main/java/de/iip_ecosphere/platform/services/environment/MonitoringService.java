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

package de.iip_ecosphere.platform.services.environment;

import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;

/**
 * Extended service interface to gain access to the {@link MetricsProvider}. Intended for services that perform
 * explicit or application-specific monitoring. Just apply this interface in addition to your service implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface MonitoringService extends Service {
    
    /**
     * Attaches the metrics provider.
     * 
     * @param provider the metrics provider instance
     */
    public void attachMetricsProvider(MetricsProvider provider);

    /**
     * Sets up a service with {@code provider}.
     * 
     * @param service the service to set up, in particular a {@link MonitoringService} or one if its subtypes
     * @param provider the provider instance
     */
    public static void setUp(Service service, MetricsProvider provider) {
        if (service instanceof MonitoringService) {
            ((MonitoringService) service).attachMetricsProvider(provider);
            if (service instanceof UpdatingMonitoringService) { // well, forward...
                provider.addService((UpdatingMonitoringService) service);
            }
        }
    }
    
}
