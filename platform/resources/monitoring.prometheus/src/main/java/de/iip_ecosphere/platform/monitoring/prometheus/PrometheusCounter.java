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
import io.prometheus.client.Counter;

public class PrometheusCounter {
    //Beispiel für einen Counter
    static final Counter REQUESTS = Counter.build()
        .name("requests_total").help("Total requests.").register();
    static final Counter FAILEDREQUESTS = Counter.build()
        .name("requests_failed_total").help("Total failed requests.").register();

    /** Runs the sample process.
     * 
     */
    void processRequest() {
        REQUESTS.inc();
    }
}
