package de.iip_ecosphere.platform.monitoring.prometheus;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.prometheus.client.Adapter;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Predicate;

import io.prometheus.client.servlet.common.exporter.Exporter;
import io.prometheus.client.servlet.common.exporter.ServletConfigurationException;

import static io.prometheus.client.Adapter.wrap;

public class PrometheusMetricsServlet extends HttpServlet {
    /**
     * serial id.
     * 
     */
    private static final long serialVersionUID = 1L;
    private final Exporter exporter;

    /** Default constructor.
     * 
     */
    public PrometheusMetricsServlet() {
        this(CollectorRegistry.defaultRegistry, null);
    }
    /** Constructor with namefilter.
     * 
     * @param sampleNameFilter
     */
    public PrometheusMetricsServlet(Predicate<String> sampleNameFilter) {
        this(CollectorRegistry.defaultRegistry, sampleNameFilter);
    }

    /** Constructor with registry.
     * 
     * @param registry
     */
    public PrometheusMetricsServlet(CollectorRegistry registry) {
        this(registry, null);
    }
    
    /** Constructor with Filter and Registry.
     * 
     * @param registry
     * @param sampleNameFilter
     */
    public PrometheusMetricsServlet(CollectorRegistry registry, Predicate<String> sampleNameFilter) {
        exporter = new Exporter(registry, sampleNameFilter);
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        try {
            super.init(servletConfig);
            exporter.init(Adapter.wrap(servletConfig));
        } catch (ServletConfigurationException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        exporter.doGet(wrap(req), wrap(resp));
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        exporter.doPost(wrap(req), wrap(resp));
    }
}
