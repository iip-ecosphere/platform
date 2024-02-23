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

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import iip.datatypes.LaserOutput;
import iip.datatypes.MdzhConfigOutput;
import iip.nodes.EngravingLaserOPCConnector;

//MAY BE DELETED, NOT PERMANENT !!!

/**
 * Simple, Laser OPC test.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LaserOpcTest {

    private static ReceptionCallback<LaserOutput> callback = new ReceptionCallback<LaserOutput>() {

        @Override
        public void received(LaserOutput data) {
            System.out.println("Laser: " + data);
        }

        @Override
        public Class<LaserOutput> getType() {
            return LaserOutput.class;
        }
        
    };
    
    /**
     * Starts the test.
     * 
     * @param args ignored
     * @throws IOException if connector creation fails
     */
    public static void main(String[] args) throws IOException {
        SerializerRegistry.registerSerializer(iip.serializers.MdzhConfigOutputImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.MdzhConfigOutputSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.LaserOutputImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.LaserOutputSerializer.class);
        
        de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector<LaserOutput, MdzhConfigOutput> conn = 
            new de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector<>(
                EngravingLaserOPCConnector.createConnectorAdapter());
        conn.connect(EngravingLaserOPCConnector.createConnectorParameter());
        conn.setReceptionCallback(callback);
        conn.notificationsChanged(false); // force sampling independent of model

        while (true) {
            conn.request(true);
            TimeUtils.sleep(1000);
        }
    }
    
}
