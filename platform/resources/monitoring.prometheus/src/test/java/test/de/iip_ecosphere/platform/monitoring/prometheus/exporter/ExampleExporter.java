package test.de.iip_ecosphere.platform.monitoring.prometheus.exporter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/** TestExporter.
 * 
 * @author const
 *
 */
public class ExampleExporter {
    
    /** TestMethode.
     * 
     */
    @Test
    public void test() {  
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        //MetricsProvider with PrometheusRegistry
        MetricsProvider metricsProvider = new MetricsProvider(prometheusRegistry);
        metricsProvider.calculateNonNativeSystemMetrics();
        metricsProvider.registerNonNativeSystemMetrics();
        metricsProvider.registerMemoryMetrics();
        metricsProvider.registerDiskMetrics();
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9642), 0);
            server.createContext("/prometheus", httpExchange -> {
                String response = prometheusRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });

            new Thread(server::start).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
















