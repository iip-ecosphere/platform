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
 * Specialized monitoring service that is automatically updated with the {@link MetricsProvider}.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface UpdatingMonitoringService extends MonitoringService {

    /**
     * Called to calculate/update the metrics.
     */
    public void calculateMetrics();

}
