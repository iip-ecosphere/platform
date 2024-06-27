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

package test.de.iip_ecosphere.platform.examples.python.connectivity;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.events.SimpleTimeseriesQuery;
import de.iip_ecosphere.platform.connectors.events.SimpleTimeseriesQuery.TimeKind;
import de.iip_ecosphere.platform.connectors.influx.InfluxConnector;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import iip.datatypes.InfluxEnergyData;
import iip.datatypes.InfluxEnergyDataImpl;
import iip.nodes.InfluxDbCloudConnector;

/**
 * Simple test for the influx connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public class InfluxConnectorTest {
    
    private static int received = 0;

    private static ReceptionCallback<InfluxEnergyData> callback = new ReceptionCallback<InfluxEnergyData>() {

        @Override
        public void received(InfluxEnergyData data) {
            System.out.println("Sentron: " + data);
            received++;
        }

        @Override
        public Class<InfluxEnergyData> getType() {
            return InfluxEnergyData.class;
        }

    };

    /**
     * Main program.
     * 
     * @param args ignored
     * @throws IOException shall not occur
     */
    public static void main(String[] args) throws IOException {
        ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        SerializerRegistry.registerSerializer(iip.serializers.SentronOutputSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.SentronOutputImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.EmptySerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.EmptyImplSerializer.class);

        ConnectorParameter params = InfluxDbCloudConnector.createConnectorParameter(); // or own and customize

        InfluxConnector<InfluxEnergyData, InfluxEnergyData> conn = 
            new InfluxConnector<InfluxEnergyData, InfluxEnergyData>(
                InfluxDbCloudConnector.createConnectorAdapter());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> conn.disconnectSafe()));
        conn.connect(params);
        conn.setReceptionCallback(callback);
        LoggerFactory.getLogger(EanSerialConnectorTest.class).info("Influx connector create/connected");
        InfluxEnergyData data = new InfluxEnergyDataImpl();
        data.setPowerConsumption(160);
        System.out.println("Writing data...");
        conn.write(data);
        System.out.println("Data written...");
        System.out.println("Querying data...");
        conn.trigger(new SimpleTimeseriesQuery(0, TimeKind.ABSOLUTE));
        long start = System.currentTimeMillis();
        System.out.println("Waiting/receiving data... (max 3 datapoints, max 3s)");
        while (received < 3 && (System.currentTimeMillis() - start) < 3000) {
            TimeUtils.sleep(500);
        }
        System.out.println("Records received, disconnecting");
        conn.disconnect();
    }

}
