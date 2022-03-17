package de.iip_ecosphere.platform.monitoring.prometheus;

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
