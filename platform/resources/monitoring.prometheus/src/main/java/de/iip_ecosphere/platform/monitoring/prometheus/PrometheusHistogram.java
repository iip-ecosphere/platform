package de.iip_ecosphere.platform.monitoring.prometheus;

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
