package de.iip_ecosphere.platform.monitoring.prometheus;

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
