/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.examples.hm23.connectivity;

import java.io.IOException;

import de.iip_ecosphere.platform.examples.hm23.drive.ChannelToTimeSeriesAggregator;
import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import iip.datatypes.AggregatedPlcEnergyMeasurement;
import iip.datatypes.PlcEnergyIn;
//import iip.datatypes.PlcEnergyMeasurement;
import iip.datatypes.PlcEnergyMeasurementJson;
import iip.interfaces.ChannelToTimeSeriesAggregatorInterface;
import iip.nodes.MqttEnergyConn;

//MAY BE DELETED, NOT PERMANENT !!!

/**
 * Simple, MQTT drive data test.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MqttEnergyTest {
    
    private static ChannelToTimeSeriesAggregatorInterface aggregator;

    private static ReceptionCallback<PlcEnergyMeasurementJson> callbackEnergy 
        = new ReceptionCallback<PlcEnergyMeasurementJson>() {

            @Override
            public void received(PlcEnergyMeasurementJson data) {
                System.out.println("Energy: " + data);
                //aggregator.processPlcEnergyMeasurement(data);
                aggregator.processPlcEnergyMeasurementJson(data);
            }
    
            @Override
            public Class<PlcEnergyMeasurementJson> getType() {
                return PlcEnergyMeasurementJson.class;
            }
            
        };
        
    private static DataIngestor<AggregatedPlcEnergyMeasurement> aggIngestor 
        = new DataIngestor<AggregatedPlcEnergyMeasurement>() {

            @Override
            public void ingest(AggregatedPlcEnergyMeasurement data) {
                System.out.println("Aggregated data: " + data);
            }
            
        };
    
    /**
     * Starts the test.
     * 
     * @param args ignored
     * @throws IOException if connector creation fails
     */
    public static void main(String[] args) throws IOException {
        SerializerRegistry.registerSerializer(iip.serializers.CommandImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.CommandSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.PlcOutputImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.PlcOutputSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.PlcInputImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.PlcInputSerializer.class);
        
        de.iip_ecosphere.platform.connectors.mqttv3.PahoMqttv3Connector<PlcEnergyMeasurementJson, PlcEnergyIn> 
            connDrive = new de.iip_ecosphere.platform.connectors.mqttv3.PahoMqttv3Connector<>(
                MqttEnergyConn.createConnectorAdapter());
        connDrive.connect(MqttEnergyConn.createConnectorParameter());
        connDrive.setReceptionCallback(callbackEnergy);
        connDrive.notificationsChanged(false); // force sampling independent of model

        aggregator = new ChannelToTimeSeriesAggregator();
        aggregator.attachAggregatedPlcEnergyMeasurementIngestor(aggIngestor);
        
        while (true) {
            TimeUtils.sleep(500);
            System.out.println("Waiting for data...");
        }
    }
    
}
