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
import io.micrometer.core.ipc.http.HttpSender.Request;
import io.prometheus.client.Histogram;

/** Sample for Prometheus Histogram.
 * 
 * @author const
 *
 */
public class PrometheusHistogram {
    // Beispiel für ein Histogramm
    static final Histogram REQUESTLATENCY = Histogram.build().name("requests_latency_seconds")
            .help("Request latency in seconds.").register();
    
    /** runs the sample process.
     * 
     * @param req the request.
     */
    void processRequest(Request req) {
        @SuppressWarnings("unused")
        Histogram.Timer requestTimer = REQUESTLATENCY.startTimer();
     
    }
}
