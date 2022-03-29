package de.iip_ecosphere.platform.monitoring.prometheus;
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
import javax.ws.rs.core.Request;

import io.prometheus.client.Summary;

public class PrometheusSummary {
    // Beispiel für eine Summary
    private static final Summary REQUESTLATENCY = Summary.build().name("requests_latency_seconds")
            .help("request latency in seconds").register();
    @SuppressWarnings("unused")
    private static final Summary RECEIVEDBYTES = Summary.build().name("requests_size_bytes")
            .help("request size in bytes").register();

    /** Run Sample.
     * 
     * @param req
     */
    public void processRequest(Request req) {
        @SuppressWarnings("unused")
        Summary.Timer requestTimer = REQUESTLATENCY.startTimer(); 
    }
}
