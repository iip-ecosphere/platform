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
