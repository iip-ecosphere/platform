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
import java.util.concurrent.atomic.AtomicInteger;

import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusTcpIpConnector;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.datatypes.ModbusPhoenixEEM;
import iip.datatypes.ModbusPhoenixRwEEM;
import iip.nodes.MyModbusConnExample;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Runs the generated connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public class GeneratedConnector {

    private static MetricsProvider metrics = new MetricsProvider(new SimpleMeterRegistry());

    /**
     * Runs the generated connector. Currently not integrated with test as the
     * server must be online (external, not guaranteed).
     * 
     * @param args the command line arguments, ignored
     * @throws IOException in case that the server cannot be accessed
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && "--skip".equals(args[0])) {
            // when server is set up, regex in POM may become more specific and this part
            // may be removed
            System.out.println("MODBUS Connector test");
            System.exit(0);
        }
        ActiveAasBase.setNotificationMode(NotificationMode.NONE); // disable AAS connector registration
        AtomicInteger count = new AtomicInteger(0);
        ReceptionCallback<ModbusPhoenixEEM> cb = new ReceptionCallback<>() {

            @Override
            public void received(ModbusPhoenixEEM data) {
                System.out.println("RCV " + data);
                count.incrementAndGet();
            }

            @Override
            public Class<ModbusPhoenixEEM> getType() {
                return ModbusPhoenixEEM.class;
            }

        };
        ModbusTcpIpConnector<ModbusPhoenixEEM, ModbusPhoenixRwEEM> conn = createPlatformConnector(cb);
        final int maxRequests = 10;
        for (int i = 0; i < maxRequests; i++) {
            System.out.println("REQUEST " + i);
            conn.request(true);
        }
        System.out.println("Sleeping to flush...");
        TimeUtils.sleep(1000);
        System.out.println("Disconnecting...");
        conn.disconnect();
        System.out.println("Received: " + count);
        //System.exit(0);
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
                MyModbusConnExample.createConnectorAdapter(metrics, new File("modbusTest.txt")));
        conn.connect(MyModbusConnExample.createConnectorParameter());
        conn.setReceptionCallback(callback);
        return conn;
    }

}
