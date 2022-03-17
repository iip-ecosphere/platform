package de.iip_ecosphere.platform.monitoring.prometheus;

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
