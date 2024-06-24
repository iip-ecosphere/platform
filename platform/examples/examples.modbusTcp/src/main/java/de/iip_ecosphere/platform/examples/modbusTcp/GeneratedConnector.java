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

package de.iip_ecosphere.platform.examples.modbusTcp;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusTcpIpConnector;
import de.iip_ecosphere.platform.services.environment.metricsProvider.LogRunnable;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.datatypes.ModbusPhoenixEEM;
import iip.datatypes.ModbusPhoenixRwEEM;
import iip.nodes.MyModbusConnExample;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Runs the generated connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public class GeneratedConnector {

    public static final String TOTAL_REQUEST_TIME = "totalRequestTime";
    
    private static final int MAX = 200;
    private static MetricsProvider metrics = new MetricsProvider(new SimpleMeterRegistry());
    private static ModbusServer server;
    
    private static Clock clock;
    private static LogRunnable logger;

    /**
     * Runs the generated connector. Currently not integrated with test as the
     * server must be online (external, not guaranteed).
     * 
     * @param args the command line arguments, ignored
     * @throws IOException in case that the server cannot be accessed
     */
    public static void main(String[] args) throws IOException {

        //test(false);
        performanceTest(true); // must emit .*RECEIVED.*ModbusPhoenixEEMImpl.*, see POM!
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
        AtomicInteger count = new AtomicInteger(0);
        ReceptionCallback<ModbusPhoenixEEM> cb = new ReceptionCallback<>() {

            @Override
            public void received(ModbusPhoenixEEM data) {
                System.out.println("RECEIVED (" + count.get() + "): " + data);
                count.incrementAndGet();
            }

            @Override
            public Class<ModbusPhoenixEEM> getType() {
                return ModbusPhoenixEEM.class;
            }

        };
        ModbusTcpIpConnector<ModbusPhoenixEEM, ModbusPhoenixRwEEM> conn = createPlatformConnector(cb);

        boolean run = true;
        
        do {
            
            conn.request(true);
            TimeUtils.sleep(3);
            
        } while(run);

        System.out.println("Sleeping to flush...");
        TimeUtils.sleep(1000);
        System.out.println("Disconnecting...");
        conn.disconnect();
        System.out.println("Received: " + count);
        
        if (startServer) {
            server.stop();
        }
    }
    
    /**
     * Try to receive Data from the connected machine until MAX is reached and logs the totalRequestTime.
     * 
     * @param startServer true = Start with TestServer, false = Start without TestServer
     * @throws IOException if the connector fails (preliminary)
     */
    public static void performanceTest(boolean startServer) throws IOException {
        
        clock = metrics.getClock();
        File log = new File("totalRequestTimeGeneratedConnector.txt");
        logger = new LogRunnable(log);
        new Thread(logger).start();
        
        if (startServer) {
            server = new ModbusServer(MyModbusConnExample.createConnectorParameter());
            server.start();
        }
        

        /*
        if (args.length > 0 && "--skip".equals(args[0])) {
            // when server is set up, regex in POM may become more specific and this part
            // may be removed
            System.out.println("MODBUS Connector test");
            System.exit(0);
        }
        */
        
        ActiveAasBase.setNotificationMode(NotificationMode.NONE); // disable AAS connector registration
        AtomicInteger count = new AtomicInteger(0);
        ReceptionCallback<ModbusPhoenixEEM> cb = new ReceptionCallback<>() {

            @Override
            public void received(ModbusPhoenixEEM data) {
                System.out.println("RECEIVED (" + count.get() + "): " + data);
                count.incrementAndGet();
            }

            @Override
            public Class<ModbusPhoenixEEM> getType() {
                return ModbusPhoenixEEM.class;
            }

        };
        ModbusTcpIpConnector<ModbusPhoenixEEM, ModbusPhoenixRwEEM> conn = createPlatformConnector(cb);

        for (int i = 0; i < MAX; i++) {
            
            final long s = clock.monotonicTime();
            conn.request(true);
            final long duration = clock.monotonicTime() - s;
            metrics.recordWithTimer(TOTAL_REQUEST_TIME, duration, TimeUnit.NANOSECONDS);
            logger.log(TOTAL_REQUEST_TIME, duration);
            
            
            TimeUtils.sleep(3);
        }

        System.out.println("Sleeping to flush...");
        TimeUtils.sleep(1000);
        System.out.println("Disconnecting...");
        conn.disconnect();
        System.out.println("Received: " + count);

        if (startServer) {
            server.stop();
        }
        logger.stop();
    }

    /**
     * Creates the platform connector to be tested.
     * 
     * @param callback the callback
     * @return the connector instance
     * @throws IOException if creating the connector fails
     */
    public static ModbusTcpIpConnector<ModbusPhoenixEEM, ModbusPhoenixRwEEM> createPlatformConnector(
            ReceptionCallback<ModbusPhoenixEEM> callback) throws IOException {
        ModbusTcpIpConnector<ModbusPhoenixEEM, ModbusPhoenixRwEEM> conn = new ModbusTcpIpConnector<>(
                //MyModbusConnExample.createConnectorAdapter(metrics, new File("modbusTestGeneratedConnector.txt")));
                MyModbusConnExample.createConnectorAdapter());
        conn.connect(MyModbusConnExample.createConnectorParameter());
        conn.setReceptionCallback(callback);
        return conn;
    }

}
