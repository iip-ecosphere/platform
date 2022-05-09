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

package de.iip_ecosphere.platform.transport.streams;

import de.iip_ecosphere.platform.transport.status.Alert;
import de.iip_ecosphere.platform.transport.status.StatusMessage;
import de.iip_ecosphere.platform.transport.status.TraceRecord;

/**
 * Global platform stream names (see handbook, forward declarations).
 * 
 * @author Holger Eichelberger, SSE
 */
public class StreamNames {

    /**
     * Component status changes stream in terms of {@link StatusMessage}.
     */
    public static final String STATUS_STREAM = "ComponentStatus";

    /**
     * Optional tracing stream in terms of {@link TraceRecord}.
     */
    public static final String TRACE_STREAM = "Trace";

    /**
     * Service metrics stream (forward declaration).
     */
    public static final String SERVICE_METRICS = "ServiceMetrics";

    /**
     * Resource metrics stream (forward declaration).
     */
    public static final String RESOURCE_METRICS = "EcsMetrics";

    /**
     * Alerts in terms of {@link Alert}.
     */
    public static final String ALERTS = "Alerts";
    
}
