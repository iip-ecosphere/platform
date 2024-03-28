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

package de.iip_ecosphere.platform.examples.vdw;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.datatypes.OpcIn;
import iip.datatypes.OpcOut;
import iip.nodes.MyOpcConnExample;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Runs the generated connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public class GeneratedConnector {

    private static MetricsProvider metrics = new MetricsProvider(new SimpleMeterRegistry());

    /**
     * Runs the generated connector. Currently not integrated with test as the VDW OPC server must be online 
     * (external, not guaranteed).
     * 
     * @param args the command line arguments, ignored
     * @throws IOException in case that the VDW server cannot be accessed
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && "--skip".equals(args[0])) {
            // our Jenkins cannot contact the umati server. well let's figure out whether it could run at all
            System.out.println("UMATI OPCUA Connector test");
            System.exit(0);
        }
        ActiveAasBase.setNotificationMode(NotificationMode.NONE); // disable AAS connector registration
        AtomicInteger count = new AtomicInteger(0);
        ReceptionCallback<OpcOut> cb = new ReceptionCallback<OpcOut>() {

            @Override
            public void received(OpcOut data) {
                System.out.println("RCV " + data.getState().getMachine() + "\n" 
                    + data.getIdentification());
                count.incrementAndGet();
            }
            
            @Override
            public Class<OpcOut> getType() {
                return OpcOut.class;
            }
            
            
        };
        OpcUaConnector<OpcOut, OpcIn> conn = createPlatformConnector(cb);
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
        System.exit(0);
    }

    /**
     * Creates the platform connector to be tested.
     *  
     * @param callback the callback
     * @return the connector instance
     * @throws IOException if creating the connector fails
     */
    public static OpcUaConnector<OpcOut, OpcIn> createPlatformConnector(
        ReceptionCallback<OpcOut> callback) throws IOException {
        OpcUaConnector<OpcOut, OpcIn> conn = new OpcUaConnector<>(
            MyOpcConnExample.createConnectorAdapter(metrics, new File("opcTest.txt")));
        conn.connect(MyOpcConnExample.createConnectorParameter());
        conn.setReceptionCallback(callback);
        return conn;
    }

}
