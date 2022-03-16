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

package de.iip_ecosphere.platform.services.environment.metricsProvider;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.types.ChannelTranslatingProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorInputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import io.micrometer.core.instrument.Clock;

/**
 * Implements a monitoring wrapper for {@link TranslatingProtocolAdapter} to be instrumented into by generation.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public class MonitoredTranslatingProtocolAdapter<O, I, CO, CI> extends ChannelTranslatingProtocolAdapter<O, I, CO, CI> {

    public static final String ADAPT_INPUT_TIME = "adaptInputTime";
    public static final String ADAPT_OUTPUT_TIME = "adaptOutputTime";
    private MetricsProvider metrics;
    private Clock clock;
    private LogRunnable logger;

    /**
     * Creates a monitored translating protocol adapter with empty channels.
     * 
     * @param outputTranslator the output translator
     * @param inputTranslator the input translator
     * @param metrics the metrics provider used to measure
     * @param log optional file to log individual values to (may be <b>null</b> for none)
     */
    public MonitoredTranslatingProtocolAdapter(ConnectorOutputTypeTranslator<O, CO> outputTranslator, 
            ConnectorInputTypeTranslator<CI, I> inputTranslator, MetricsProvider metrics, File log) {
        this("", outputTranslator, "", inputTranslator, metrics, log);
    }
    
    // checkstyle: stop parameter number check
    
    /**
     * Creates a monitored translating protocol adapter.
     * 
     * @param outputChannel the name of the input channel. Further semantics is 
     *   implied/restrictions are imposed by the underlying protocol.
     * @param outputTranslator the output translator
     * @param inputChannel the name of the input channel. Further semantics is 
     *   implied/restrictions are imposed by the underlying protocol.
     * @param inputTranslator the input translator
     * @param metrics the metrics provider used to measure
     * @param log optional file to log individual values to (may be <b>null</b> for none)
     */
    public MonitoredTranslatingProtocolAdapter(String outputChannel, 
        ConnectorOutputTypeTranslator<O, CO> outputTranslator, String inputChannel,
        ConnectorInputTypeTranslator<CI, I> inputTranslator, MetricsProvider metrics, File log) {
        super(outputChannel, outputTranslator, inputChannel, inputTranslator);
        this.metrics = metrics;
        this.clock = metrics.getClock();
        if (null != log) {
            try {
                logger = new LogRunnable(log);
                new Thread(logger).start();
            } catch (IOException e) {
                logger = null;
                LoggerFactory.getLogger(MonitoredTranslatingProtocolAdapter.class).error("Cannot create log file " 
                    + logger + ": " + e.getMessage());
            }
        }
    }

    // checkstyle: resume parameter number check

    /**
     * Logs an activity if there is a logger.
     * 
     * @param activity the activity name
     * @param duration the duration
     */
    private void log(String activity, long duration) {
        if (null != logger) {
            logger.log(activity, duration);
        }
    }
    
    @Override
    public I adaptInput(final CI data) throws IOException {
        // no obvious way to combine lambda with super, measurement from micrometer
        final long s = clock.monotonicTime();
        try {
            return super.adaptInput(data);
        } finally {
            final long duration = clock.monotonicTime() - s;
            metrics.recordWithTimer(ADAPT_INPUT_TIME, duration, TimeUnit.NANOSECONDS);
            log(ADAPT_INPUT_TIME, duration);
        }
    }

    @Override
    public CO adaptOutput(O data) throws IOException {
        // no obvious way to combine lambda with super, measurement from micrometer
        final long s = clock.monotonicTime();
        try {
            return super.adaptOutput(data);
        } finally {
            final long duration = clock.monotonicTime() - s;
            metrics.recordWithTimer(ADAPT_OUTPUT_TIME, duration, TimeUnit.NANOSECONDS);
            log(ADAPT_OUTPUT_TIME, duration);
        }
    }

}
