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
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import de.oktoflow.platform.connectors.serial.JSerialCommConnector;
import iip.datatypes.EanScannerOutput;
import iip.datatypes.Empty;
import iip.nodes.SerialEANCodeScanner;

/**
 * Tests the connection to the serial EAN scanner.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EanSerialConnectorTest {

    private static ReceptionCallback<EanScannerOutput> callback 
        = new ReceptionCallback<EanScannerOutput>() {
    
            @Override
            public void received(EanScannerOutput data) {
                System.out.println("Scanner: " + data.getData());
            }
    
            @Override
            public Class<EanScannerOutput> getType() {
                return EanScannerOutput.class;
            }
            
        };

    /**
     * Main program.
     * 
     * @param args ignored
     * @throws IOException shall not occur
     */
    public static void main(String[] args) throws IOException {
        SerializerRegistry.registerSerializer(iip.serializers.EanScannerOutputSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.EanScannerOutputImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.EmptySerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.EmptyImplSerializer.class);

        ConnectorParameter params = SerialEANCodeScanner
            .createConnectorParameter(); // or own and customize
        
        JSerialCommConnector<EanScannerOutput, Empty> conn = new JSerialCommConnector<>(
            SerialEANCodeScanner.createConnectorAdapter());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> conn.disconnectSafe()));
        conn.connect(params);
        conn.setReceptionCallback(callback);
        LoggerFactory.getLogger(EanSerialConnectorTest.class).info("Serial EAN connector created");

        // wait for data
        while (true) {
            TimeUtils.sleep(300);
        }
    }
    
}
