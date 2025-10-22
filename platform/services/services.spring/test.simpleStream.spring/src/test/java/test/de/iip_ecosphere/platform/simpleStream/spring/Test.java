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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.services.environment.spring.SpringAsyncServiceBase;
import de.iip_ecosphere.platform.services.environment.spring.Starter;
import de.iip_ecosphere.platform.services.environment.YamlService;

import de.iip_ecosphere.platform.services.environment.spring.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.metrics.Counter;
import de.iip_ecosphere.platform.support.metrics.Timer;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.setup.CmdLine;
import de.iip_ecosphere.platform.transport.Transport;

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
    
    //private static final String SUPPLIER_TIMER_ID = "suppliercustomtimer";
    private static final String SUPPLIER_GAUGE_ID = "suppliercustomgauge";
    private static final String SUPPLIER_COUNTER_ID = "suppliercustomcounter";

    //private static final String CONSUMER_TIMER_ID = "consumercustomtimer";
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
    
    private Counter logSent;
    private Counter logReceived;
    private Timer logTime;
    private Counter createSent;
    private Counter createReceived;
    private Timer createTime;
    private SpringAsyncServiceBase base = new SpringAsyncServiceBase();
    private String appInstId = "";
    
    /**
     * Creates an instance.
     * 
     * @param environment the Spring environment
     */
    @Autowired
    public Test(Environment environment) {
        super(environment);
        appInstId = getAppInstIdSuffix("_");
    }
    
    /**
     * Composes a channel suffix id for a service possibly including the application instance id.
     * 
     * @param separator the separator string to insert between the ids
     * @return the id suffix, may be empty
     */
    public static String getAppInstIdSuffix(String separator) {
        String result;
        String sId = Starter.getServiceId("create"); // we just need application instance id
        result = ServiceBase.getApplicationInstanceId(sId);
        if (null == result || result.length() == 0) {
            result = "";
        } else {
            result = separator + result;
        }
        return result;
    }
    
    /**
     * Creates the data.
     * 
     * @return the data (generator)
     */
    @Bean
    public Supplier<String> create() {
        return () -> {
            return createTime.record(() -> {
                createReceived.increment();
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
                    stop();
                    System.exit(0);
                }
                if (config.getIngestCount() > 0) {
                    ingestCount++;
                }
                createSent.increment();
                ingest(String.valueOf(num)); // also do async
                return String.valueOf(num);
            });
        };
    }

    /**
     * Stops the application.
     */
    public static void stop() {
        if (null != getContext()) {
            getContext().close();
        }        
    }
    
    /**
     * Consumes the received data.
     * 
     * @return the data receiver
     */
    @Bean
    public Consumer<String> log() {
        return data -> {
            logReceived.increment();
            logTime.record(() -> {
                double num = Math.random();
                String content = "Received: " + data + "\n";
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
                logSent.increment();
            });
        };
    }

    /**
     * Simplified logging reception for async calls. Non-spring, no metrics.
     * 
     * @return the data receiver
     */
    private Consumer<String> logAsync() {
        return data -> {
            logReceived.increment();
            logTime.record(() -> {
                String content = "Received-Async: " + data + "\n";
                if (config.isDebug()) {
                    System.out.println("Received-Async: " + data);
                }
                try {
                    Files.write(Paths.get(fileName), content.getBytes(), 
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    System.out.println("Error writing log: " + e.getMessage());
                }
            });
        };
    }
    
    /**
     * Called after constructor.
     */
    @PostConstruct
    public void postConstruct() {
        String logId = "simpleStream-log";
        String createId = "simpleStream-create";
        String app = "simpleStream.spring";
        logSent = metrics.createServiceSentCounter(logId, logId, app, null);
        logReceived = metrics.createServiceReceivedCounter(logId, logId, app, null);
        logTime = metrics.createServiceProcessingTimer(logId, logId, app, null);
        createSent = metrics.createServiceSentCounter(createId, createId, app, null);
        createReceived = metrics.createServiceReceivedCounter(createId, createId, app, null);
        createTime = metrics.createServiceProcessingTimer(createId, createId, app, null);
        base.createReceptionCallback("data_logAsync" + appInstId, logAsync(), String.class, "log-in-0");
    }
    
    /**
     * Called during shutdown.
     */
    @PreDestroy
    public void destroy() {
        base.detach();
    }

    /**
     * Asynchronously ingests data.
     * 
     * @param data the data to ingest
     */
    private void ingest(Object data) {
        Transport.send(c -> c.asyncSend("data_logAsync" + appInstId, data), "SimpleSource", "log-in-0"); 
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
        ResourceLoader.addTestExcludeFilters();
        fileName = CmdLine.getArg(args, "test.log", FileUtils.getTempDirectoryPath() + "/test.simpleStream.spring.log");
        FileUtils.deleteQuietly(new File(fileName));
        Starter.main(Test.class, args);
    }
    
}
