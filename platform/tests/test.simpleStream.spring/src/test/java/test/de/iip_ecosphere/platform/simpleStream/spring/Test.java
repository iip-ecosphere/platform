/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package test.de.iip_ecosphere.platform.simpleStream.spring;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.services.environment.spring.Starter;
import de.iip_ecosphere.platform.services.environment.YamlService;

import de.iip_ecosphere.platform.services.environment.spring.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;

/**
 * Defines the test stream to be processed. We assume that a broker is running.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan({"test.de.iip_ecosphere.platform.simpleStream.spring", 
    "de.iip_ecosphere.platform.services.environment.spring", "de.iip_ecosphere.platform.transport.spring"})
public class Test extends Starter {
    
    private static final String SUPPLIER_TIMER_ID = "suppliercustomtimer";
    private static final String SUPPLIER_GAUGE_ID = "suppliercustomgauge";
    private static final String SUPPLIER_COUNTER_ID = "suppliercustomcounter";

    private static final String CONSUMER_TIMER_ID = "consumercustomtimer";
    private static final String CONSUMER_GAUGE_ID = "consumercustomgauge";
    private static final String CONSUMER_COUNTER_ID = "consumercustomcounter";
    private static final String CONSUMER_RECV_ID = "consumerreceptiongauge";

    private static final String REST_GAUGE_ID = "restgauge";
    private static final String REST_COUNTER_ID = "restcounter";
    private static final String REST_TIMER_ID = "resttimer";
    
    private static String fileName;
    
    @Autowired
    private Configuration config;
    private int ingestCount = 0;
    @Autowired
    private MetricsProvider metrics;
    private boolean first = true;
    
    /**
     * Creates an instance.
     * 
     * @param environment the Spring environment
     */
    @Autowired
    public Test(Environment environment) {
        super(environment);
    }
    
    /**
     * Creates the data.
     * 
     * @return the data (generator)
     */
    @Bean
    public Supplier<String> create() {
        return () -> {
            return metrics.recordWithTimer(SUPPLIER_TIMER_ID, () -> {
                if (config.isDebug()) {
                    System.out.println("Ingest " + ingestCount);
                }
                double num = Math.random();
                metrics.addGaugeValue(SUPPLIER_GAUGE_ID, num);
                metrics.increaseCounter(SUPPLIER_COUNTER_ID);
                metrics.increaseCounterBy(SUPPLIER_COUNTER_ID, num);
                if (first) {
                    metrics.addGaugeValue(REST_GAUGE_ID, 0);
                    metrics.increaseCounterBy(REST_COUNTER_ID, 0);
                    metrics.recordWithTimer(REST_TIMER_ID, 0, TimeUnit.MILLISECONDS);
                    first = false;
                } else if (config.getIngestCount() > 0 && ingestCount > config.getIngestCount()) {
                    if (null != getContext()) {
                        getContext().close();
                    }
                    System.exit(0);
                }
                if (config.getIngestCount() > 0) {
                    ingestCount++;
                }
                return String.valueOf(num);
            });
        };
    }
    
    /**
     * Consumes the received data.
     * 
     * @return the data receiver
     */
    @Bean
    public Consumer<String> log() {
        return data -> {
            metrics.recordWithTimer(CONSUMER_TIMER_ID, () -> {
                double num = Math.random();
                String content = "Received: " + data;
                if (config.isDebug()) {
                    System.out.println("Received: " + data);
                }
                metrics.addGaugeValue(CONSUMER_GAUGE_ID, num);
                metrics.increaseCounter(CONSUMER_COUNTER_ID);
                metrics.increaseCounterBy(CONSUMER_COUNTER_ID, num);
                metrics.addGaugeValue(CONSUMER_RECV_ID, Double.valueOf(data));
                try {
                    Files.write(Paths.get(fileName), content.getBytes(), 
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    System.out.println("Error writing log: " + e.getMessage());
                }
            });
        };
    }

    @Override
    protected List<Service> createServices(YamlArtifact artifact) {
        List<Service> result = new ArrayList<Service>();
        for (YamlService service : artifact.getServices()) {
            result.add(new TestService(service));
        }
        return result;
    }
    
    /**
     * Main function.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        fileName = CmdLine.getArg(args, "test.log", FileUtils.getTempDirectoryPath() + "/test.simpleStream.spring.log");
        FileUtils.deleteQuietly(new File(fileName));
        Starter.main(Test.class, args);
    }
    
}
