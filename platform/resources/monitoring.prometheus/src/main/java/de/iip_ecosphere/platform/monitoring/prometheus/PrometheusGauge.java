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
import io.prometheus.client.Gauge;

public class PrometheusGauge {
    // Beispiel für eine Gauge
    static final Gauge INPROGRESSREQUESTS = Gauge.build().name("inprogress_requests").help("Inprogress requests.")
            .labelNames("method").register();
 
    /** Get request.
     * 
     */
    void processGetRequest() {
        INPROGRESSREQUESTS.labels("get").inc();
        // Do Something.
        INPROGRESSREQUESTS.labels("get").dec();
    }

    /** Post Request.
     * 
     */
    void processPostRequest() {
        INPROGRESSREQUESTS.labels("post").inc();
        // Do Something.
        INPROGRESSREQUESTS.labels("post").dec();
    }
}
