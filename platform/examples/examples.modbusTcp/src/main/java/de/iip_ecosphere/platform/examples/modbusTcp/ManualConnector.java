/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.modbusTcp;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusItem;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusTcpIpConnector;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.services.environment.metricsProvider.LogRunnable;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.nodes.MyModbusConnExample;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Simple app to run the connector.
 *
 * @author Holger Eichelberger, SSE
 */
public class ManualConnector {

    public static final String TOTAL_REQUEST_TIME = "totalRequestTime";
    
    private static final int MAX = 100;
    private static MetricsProvider metrics = new MetricsProvider(new SimpleMeterRegistry());
    private static ModbusServer server;
    
    private static Clock clock;
    private static LogRunnable logger;


    /**
     * Creates a ManualConnector instance and returns it.
     * 
     * @return a ManualConnector instance
     * 
     */
    public static Connector<ModbusItem, Object, ModbusDataE, ModbusCommandE> createConnector() {

        Connector<ModbusItem, Object, ModbusDataE, ModbusCommandE> connector = 
                new ModbusTcpIpConnector<ModbusDataE, ModbusCommandE>(
                createConnectorAdapter(null, null, null, null));

        return connector;
    }

    /**
     * Creates the connector adapter. [public for testing]
     * 
     * @param metrics         the metrics provider to use, <b>null</b> for no metric
     *                        measurements
     * @param log             the log file to use to record individual measurements
     *                        in experiments, <b>null</b> for no logging
     * @param inPathSupplier  function returning the actual input base path to use
     *                        for data accesses, may be <b>null</b>
     * @param outPathSupplier function returning the actual output base path to use
     *                        for data accesses, may be <b>null</b>
     * @return the connector adapter
     */
    public static TranslatingProtocolAdapter<ModbusItem, Object, ModbusDataE, ModbusCommandE> 
        createConnectorAdapter(
            MetricsProvider metrics, File log, Supplier<String> inPathSupplier, Supplier<String> outPathSupplier) {

        TranslatingProtocolAdapter<ModbusItem, Object, ModbusDataE, ModbusCommandE> adapter;
        adapter = new TranslatingProtocolAdapter<ModbusItem, Object, ModbusDataE, ModbusCommandE>(
                new ModbusDataEOutputTranslator<ModbusItem>(false, ModbusItem.class),
                new ModbusCommandEInputTranslator<Object>(Object.class));

        return adapter;

    }

    /**
     * Executes the connector.
     * 
     * @param args ignored
     * @throws IOException if the connector fails (preliminary)
     */
    public static void main(String... args) throws IOException {

        //test(true);
        performanceTest(true);

    }
    
    /**
     * Try to receive Data from the connected machine until the user ends the test.
     * 
     * @param startServer true = Start with TestServer, false = Start without TestServer
     * @throws IOException if the connector fails (preliminary)
     */
    public static void test(boolean startServer) throws IOException {
        
        if (startServer) {
            server = new ModbusServer(MyModbusConnExample.createConnectorParameter());
            server.start();
        }
        
        ActiveAasBase.setNotificationMode(NotificationMode.NONE); // disable AAS connector registration

        AtomicReference<ModbusDataE> md = new AtomicReference<ModbusDataE>();
        AtomicInteger count = new AtomicInteger(0);

        Connector<ModbusItem, Object, ModbusDataE, ModbusCommandE> connector = ManualConnector
                .createConnector();
        
        connector.setReceptionCallback(new ReceptionCallback<ModbusDataE>() {

            @Override
            public void received(ModbusDataE data) {
                System.out.println("RECEIVED (" + count.get() + "): " + data.toString());
                md.set(data);
                count.incrementAndGet();
            }

            @Override
            public Class<ModbusDataE> getType() {
                return ModbusDataE.class;
            }

        });

        connector.connect(MyModbusConnExample.createConnectorParameter());
        
        boolean run = true;
        
        do {
            
            connector.request(true);
            TimeUtils.sleep(3);
 
        } while (run);
        
        System.out.println("Sleeping to flush...");
        TimeUtils.sleep(1000);
        System.out.println("Disconnecting...");
        connector.disconnect();
        System.out.println("Received: " + count);
        
        if (startServer) {
            server.stop();
        }  
    }
    
    /**
     *  Try to receive Data from the connected machine until MAX is reached and logs the totalRequestTime.
     * 
     * @param startServer true = Start with TestServer, false = Start without TestServer
     * @throws IOException if the connector fails (preliminary)
     */
    public static void performanceTest(boolean startServer) throws IOException {
        clock = metrics.getClock();
        File log = new File("totalRequestTimeManualConnector.txt");
        logger = new LogRunnable(log);
        new Thread(logger).start();
    
        if (startServer) {
            server = new ModbusServer(MyModbusConnExample.createConnectorParameter());
            server.start();
        }
        
        ActiveAasBase.setNotificationMode(NotificationMode.NONE); // disable AAS connector registration

        AtomicReference<ModbusDataE> md = new AtomicReference<ModbusDataE>();
        AtomicInteger count = new AtomicInteger(0);

        Connector<ModbusItem, Object, ModbusDataE, ModbusCommandE> connector = ManualConnector
                .createConnector();

        connector.setReceptionCallback(new ReceptionCallback<ModbusDataE>() {

            @Override
            public void received(ModbusDataE data) {
                System.out.println("RECEIVED (" + count.get() + "): " + data.toString());
                md.set(data);
                count.incrementAndGet();
            }

            @Override
            public Class<ModbusDataE> getType() {
                return ModbusDataE.class;
            }

        });

        connector.connect(MyModbusConnExample.createConnectorParameter());

        for (int i = 0; i < MAX; i++) {
            
            final long s = clock.monotonicTime();
            connector.request(true);
            final long duration = clock.monotonicTime() - s;
            metrics.recordWithTimer(TOTAL_REQUEST_TIME, duration, TimeUnit.NANOSECONDS);
            logger.log(TOTAL_REQUEST_TIME, duration);
            
            TimeUtils.sleep(3);
        }

        
        System.out.println("Sleeping to flush...");
        TimeUtils.sleep(1000);
        System.out.println("Disconnecting...");
        connector.disconnect();
        System.out.println("Received: " + count);
        
        if (startServer) {
            server.stop();
        }       
    }
}
