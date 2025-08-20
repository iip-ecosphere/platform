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

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusTcpIpConnector;
import de.iip_ecosphere.platform.services.environment.metricsProvider.LogRunnable;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
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
    
    private static final boolean USE_FREE_PORT = true;
    private static final int MAX = 100;
    private static MetricsProvider metrics = new MetricsProvider(new SimpleMeterRegistry());
    private static ModbusServer server;
    private static int serverPort = -1;
    
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
            serverPort = -1;
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
            server = new ModbusServer(adjustParameter(MyModbusConnExample.createConnectorParameter(), USE_FREE_PORT));
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
            serverPort = -1;
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
                MyModbusConnExample.createConnectorAdapter());
        conn.connect(adjustParameter(MyModbusConnExample.createConnectorParameter(), false));
        conn.setReceptionCallback(callback);
        return conn;
    }

    /**
     * Adjusts connector parameters if needed/desired, but only for the first (server) call.
     * 
     * @param params the original parameters
     * @param allocate whether a new port shall be allocated if not yet one is known
     * @return the connector parameter
     */
    public static ConnectorParameter adjustParameter(ConnectorParameter params, boolean allocate) {
        if (serverPort < 0 && allocate) {
            serverPort = NetUtils.getEphemeralPort();
        }
        if (serverPort > 0) {
            return ConnectorParameter.ConnectorParameterBuilder.newBuilder(
                params, "127.0.0.1", serverPort, Schema.IGNORE).build();
        } else {
            return params;
        }
    }

}
